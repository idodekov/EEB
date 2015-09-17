package org.redoubt.cert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEEnveloped;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.mail.smime.SMIMEUtil;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import org.redoubt.api.configuration.ICryptoHelper;

public class BCCryptoHelper implements ICryptoHelper {
	
    public boolean isEncrypted(MimeBodyPart part) throws MessagingException {
        ContentType contentType = new ContentType(part.getContentType());
        String baseType = contentType.getBaseType().toLowerCase();

        if (baseType.equalsIgnoreCase("application/pkcs7-mime")) {
            String smimeType = contentType.getParameter("smime-type");

            return ((smimeType != null) && smimeType.equalsIgnoreCase("enveloped-data"));
        }

        return false;
    }

    public boolean isSigned(MimeBodyPart part) throws MessagingException {
        ContentType contentType = new ContentType(part.getContentType());
        String baseType = contentType.getBaseType().toLowerCase();

        return baseType.equalsIgnoreCase("multipart/signed");
    }

    public String calculateMIC(MimeBodyPart part, String digest, boolean includeHeaders)
            throws GeneralSecurityException, MessagingException, IOException {
        String micAlg = convertAlgorithm(digest, true);

        MessageDigest md = MessageDigest.getInstance(micAlg, "BC");

        // convert the Mime data to a byte array, then to an InputStream
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        if (includeHeaders) {
            part.writeTo(bOut);
        } else {
            IOUtil.copy(part.getInputStream(), bOut);
        }

        byte[] data = bOut.toByteArray();

        InputStream bIn = trimCRLFPrefix(data);

        // calculate the hash of the data and mime header
        DigestInputStream digIn = new DigestInputStream(bIn, md);

        byte[] buf = new byte[4096];

        while (digIn.read(buf) >= 0) {
        }

        bOut.close();

        byte[] mic = digIn.getMessageDigest().digest();
        String micString = new String(Base64.encode(mic));
        StringBuffer micResult = new StringBuffer(micString);
        micResult.append(", ").append(digest);

        return micResult.toString();
    }

    public MimeBodyPart decrypt(MimeBodyPart part, X509Certificate cert, PrivateKey key)
            throws GeneralSecurityException, MessagingException, CMSException, IOException,
            SMIMEException {
        // Make sure the data is encrypted
        if (!isEncrypted(part)) {
            throw new GeneralSecurityException("Content-Type indicates data isn't encrypted");
        }

        // Cast parameters to what BC needs
        X509Certificate x509Cert = castCertificate(cert);

        // Parse the MIME body into an SMIME envelope object
        SMIMEEnveloped envelope = new SMIMEEnveloped(part);

        // Get the recipient object for decryption
        RecipientId recId = new RecipientId();
        recId.setSerialNumber(x509Cert.getSerialNumber());
        recId.setIssuer(x509Cert.getIssuerX500Principal().getEncoded());

        RecipientInformation recipient = envelope.getRecipientInfos().get(recId);

        if (recipient == null) {
            throw new GeneralSecurityException("Certificate does not match part signature");
        }

        // try to decrypt the data
        byte[] decryptedData = recipient.getContent(key, "BC");

        return SMIMEUtil.toMimeBodyPart(decryptedData);
    }

    public void deinit() {
    }

    public MimeBodyPart encrypt(MimeBodyPart part, X509Certificate cert, String algorithm)
            throws GeneralSecurityException, SMIMEException, CMSException {
        X509Certificate x509Cert = castCertificate(cert);
        
        ASN1ObjectIdentifier encAlg = null;
        
        if(CRYPT_RC2.equals(algorithm)) {
        	encAlg = CMSAlgorithm.RC2_CBC;
        } else if(CRYPT_3DES.equals(algorithm)) {
        	encAlg = CMSAlgorithm.DES_EDE3_CBC;
        } else if(CRYPT_CAST5.equals(algorithm)) {
        	encAlg = CMSAlgorithm.CAST5_CBC;
        } else if(CRYPT_IDEA.equals(algorithm)) {
        	encAlg = CMSAlgorithm.IDEA_CBC;
        }

        SMIMEEnvelopedGenerator gen = new SMIMEEnvelopedGenerator();
        gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(x509Cert).setProvider("BC"));
        gen.setContentTransferEncoding("binary");
        MimeBodyPart encData = gen.generate(part, new JceCMSContentEncryptorBuilder(encAlg).setProvider("BC").build());
        
        return encData;
    }

    public void init() {
        Security.addProvider(new BouncyCastleProvider());

        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_signature");
        mc.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_mime");
        mc.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_signature");
        mc.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_mime");
        mc.addMailcap("multipart/signed;; x-java-content-handler=org.bouncycastle.mail.smime.handlers.multipart_signed");
        CommandMap.setDefaultCommandMap(mc);
    }

    public MimeBodyPart sign(MimeBodyPart part, X509Certificate cert, PrivateKey key, String digest)
            throws GeneralSecurityException, SMIMEException, MessagingException, OperatorCreationException {
        X509Certificate x509Cert = castCertificate(cert);
        PrivateKey privKey = castKey(key);
        
        List<X509Certificate> certList = new ArrayList<X509Certificate>();

        certList.add(x509Cert);

        Store certs = new JcaCertStore(certList);
        
        ASN1EncodableVector         signedAttrs = new ASN1EncodableVector();
        SMIMECapabilityVector       caps = new SMIMECapabilityVector();

        caps.addCapability(SMIMECapability.dES_EDE3_CBC);
        caps.addCapability(SMIMECapability.rC2_CBC, 128);
        caps.addCapability(SMIMECapability.dES_CBC);
        
        signedAttrs.add(new SMIMECapabilitiesAttribute(caps));
        
        IssuerAndSerialNumber issAndSer = new IssuerAndSerialNumber(new X500Name(x509Cert.getIssuerDN().getName()), x509Cert.getSerialNumber());
        signedAttrs.add(new SMIMEEncryptionKeyPreferenceAttribute(issAndSer));
        
        SMIMESignedGenerator gen = new SMIMESignedGenerator();
        gen.setContentTransferEncoding("binary");
        gen.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider("BC").setSignedAttributeGenerator(new AttributeTable(signedAttrs)).build("SHA1withRSA", privKey, x509Cert));
        gen.addCertificates(certs);
        MimeMultipart mm = gen.generate(part);
        
        MimeBodyPart tempBody = new MimeBodyPart();
        tempBody.setContent(mm);
        tempBody.setHeader("Content-Type", mm.getContentType());
        

        return tempBody;
    }

    public MimeBodyPart verify(MimeBodyPart part, X509Certificate cert)
            throws GeneralSecurityException, IOException, MessagingException, CMSException {
        // Make sure the data is signed
        if (!isSigned(part)) {
            throw new GeneralSecurityException("Content-Type indicates data isn't signed");
        }

        X509Certificate x509Cert = castCertificate(cert);

        MimeMultipart mainParts = (MimeMultipart) part.getContent();

        SMIMESigned signedPart = new SMIMESigned(mainParts);

        Iterator signerIt = signedPart.getSignerInfos().getSigners().iterator();
        SignerInformation signer;

        while (signerIt.hasNext()) {
            signer = (SignerInformation) signerIt.next();

            if (!signer.verify(x509Cert, "BC")) {
                throw new SignatureException("Verification failed");
            }
        }

        return signedPart.getContent();
    }

    protected X509Certificate castCertificate(Certificate cert) throws GeneralSecurityException {
        if (cert == null) {
            throw new GeneralSecurityException("Certificate is null");
        }
        if (!(cert instanceof X509Certificate)) {
            throw new GeneralSecurityException("Certificate must be an instance of X509Certificate");
        }

        return (X509Certificate) cert;
    }

    protected PrivateKey castKey(Key key) throws GeneralSecurityException {
        if (!(key instanceof PrivateKey)) {
            throw new GeneralSecurityException("Key must implement PrivateKey interface");
        }

        return (PrivateKey) key;
    }
    
    protected String convertAlgorithm(String algorithm, boolean toBC)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NoSuchAlgorithmException("Algorithm is null");
        }

        if (toBC) {
            if (algorithm.equalsIgnoreCase(DIGEST_MD5)) {
                return SMIMESignedGenerator.DIGEST_MD5;
            } else if (algorithm.equalsIgnoreCase(DIGEST_SHA1)) {
                return SMIMESignedGenerator.DIGEST_SHA1;
            } else if (algorithm.equalsIgnoreCase(CRYPT_3DES)) {
                return SMIMEEnvelopedGenerator.DES_EDE3_CBC;
            } else if (algorithm.equalsIgnoreCase(CRYPT_CAST5)) {
                return SMIMEEnvelopedGenerator.CAST5_CBC;
            } else if (algorithm.equalsIgnoreCase(CRYPT_IDEA)) {
                return SMIMEEnvelopedGenerator.IDEA_CBC;
            } else if (algorithm.equalsIgnoreCase(CRYPT_RC2)) {
                return SMIMEEnvelopedGenerator.RC2_CBC;
            } else {
                throw new NoSuchAlgorithmException("Unknown algorithm: " + algorithm);
            }
        }
        if (algorithm.equalsIgnoreCase(SMIMESignedGenerator.DIGEST_MD5)) {
            return DIGEST_MD5;
        } else if (algorithm.equalsIgnoreCase(SMIMESignedGenerator.DIGEST_SHA1)) {
            return DIGEST_SHA1;
        } else if (algorithm.equalsIgnoreCase(SMIMEEnvelopedGenerator.CAST5_CBC)) {
            return CRYPT_CAST5;
        } else if (algorithm.equalsIgnoreCase(SMIMEEnvelopedGenerator.DES_EDE3_CBC)) {
            return CRYPT_3DES;
        } else if (algorithm.equalsIgnoreCase(SMIMEEnvelopedGenerator.IDEA_CBC)) {
            return CRYPT_IDEA;
        } else if (algorithm.equalsIgnoreCase(SMIMEEnvelopedGenerator.RC2_CBC)) {
            return CRYPT_RC2;
        } else {
            throw new NoSuchAlgorithmException("Unknown algorithm: " + algorithm);
        }

    }

    protected InputStream trimCRLFPrefix(byte[] data) {
        ByteArrayInputStream bIn = new ByteArrayInputStream(data);

        int scanPos = 0;
        int len = data.length;

        while (scanPos < (len - 1)) {
            if (new String(data, scanPos, 2).equals("\r\n")) {
                bIn.read();
                bIn.read();
                scanPos += 2;
            } else {
                return bIn;
            }
        }

        return bIn;
    }

}