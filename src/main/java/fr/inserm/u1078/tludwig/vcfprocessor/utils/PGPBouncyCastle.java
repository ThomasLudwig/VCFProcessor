package fr.inserm.u1078.tludwig.vcfprocessor.utils;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Random;

public class PGPBouncyCastle {

  public static void start(){
    Security.addProvider(new BouncyCastleProvider());
  }

  public static String getSession(){
    String pool = "0123456789" +
        "AZERTYUIOP" +
        "QSDFGHJKLM" +
        "WXCVBN";
    Random random = new SecureRandom();
    char[] session = new char[12];
    for(int i = 0; i < session.length; i++)
      session[i] = pool.charAt(random.nextInt(pool.length()));
    return String.valueOf(session);
  }

  public static String createPassPhrase(){
    String first = "first";
    String second = "second";
    int tries = 5;
    while(tries-- > 0){
      first = readPassPhrase("Please create a passphrase for this encryption key");
      second = readPassPhrase("Repeat that passphrase");
      if(first.equals(second))
        return first;
      System.err.println("********************");
      System.err.println("Passphrases mismatch");
      System.err.println("********************");
    }

    Message.fatal("Failed to create a passphrase", true);

    return "";
  }

  public static String askPassPhrase(){
    return readPassPhrase("Please enter the passphrase for this encryption key");
  }

  public static String readPassPhrase(String prompt){
    Console console = System.console();
    if (console == null) {
      throw new IllegalStateException(
          "No console available (are you running from an IDE or redirected stdin?)"
      );
    }
    return String.copyValueOf(console.readPassword("%s", prompt+": "));
  }

  public static void writeEncryptedFile(String filename, byte[] data) {
    start();
    try {
      doWriteEncrypted(data,createPassPhrase(), filename);
    } catch(Exception e){
      Message.fatal("Could not write encrypted file ["+filename+"]", e, true);
    }
  }

  public static byte[] readEncryptedFile(String privateKeyFile, String filename) {
    start();
    try {
      PGPSecretKeyRingCollection secrets = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(Files.newInputStream(Path.of(privateKeyFile))), new JcaKeyFingerprintCalculator());

      int tries = 3;
      PGPPrivateKey privateKey = null;
      while(tries-- > 0){
        try {
          privateKey = extractPrivateKey(secrets, askPassPhrase().toCharArray());
        } catch(Exception e){
          System.err.println("Passphrase does not match encryption key. Try again.");
        }
      }
      if(privateKey == null)
        Message.fatal("Passphrase does not match encryption key", true);
      return doReadEncrypted(privateKey, filename);
    } catch(Exception e){
      Message.fatal("Could not read encrypted file ["+filename+"]", e, true);
      return new byte[0];
    }
  }

  public static void doWriteEncrypted(byte[] data, String passphrase, String encryptedFile) throws Exception {
    String session = getSession();
    Path pubKey = Path.of("public."+session+".pgp");
    Path secKey = Path.of("secret."+session+".pgp");

    // 1. Generate keys if missing
    if (!Files.exists(pubKey) || !Files.exists(secKey)) {
      PGPKeyRingGenerator gen = generateKeyRing("VCFProcessor@local", passphrase.toCharArray());
      Files.write(secKey, gen.generateSecretKeyRing().getEncoded());
      Files.write(pubKey, gen.generatePublicKeyRing().getEncoded());
    }

    // 2. Encrypt data using public key
    PGPPublicKey publicKey = readPublicKey(pubKey);
    byte[] encrypted = encrypt(data, publicKey);
    Files.write(Path.of(encryptedFile), encrypted);
    //Arrays.fill(passphrase, '\0');
  }

  public static byte[] doReadEncrypted(PGPPrivateKey privateKey, String encryptedFile) throws Exception {
    byte[] encrypted = Files.readAllBytes(Path.of(encryptedFile));
    return decrypt(encrypted, privateKey);
  }


  public static PGPKeyRingGenerator generateKeyRing(String identity, char[] passphrase) throws Exception {

    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
    kpg.initialize(4096);
    KeyPair kp = kpg.generateKeyPair();

    PGPKeyPair pgpKeyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, kp, new Date());

    PGPDigestCalculator sha1 =
        new JcaPGPDigestCalculatorProviderBuilder()
            .build()
            .get(HashAlgorithmTags.SHA1);

    PBESecretKeyEncryptor encryptor =
        new JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha1)
            .setProvider("BC")
            .build(passphrase);

    return new PGPKeyRingGenerator(
        PGPSignature.POSITIVE_CERTIFICATION,
        pgpKeyPair,
        identity,
        sha1,
        null,
        null,
        new JcaPGPContentSignerBuilder(pgpKeyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1),
        encryptor
    );
  }

  public static PGPPublicKey readPublicKey(Path path) throws Exception {
    try (InputStream in = PGPUtil.getDecoderStream(Files.newInputStream(path))) {
      PGPPublicKeyRingCollection rings = new PGPPublicKeyRingCollection(in, new JcaKeyFingerprintCalculator());

      for (PGPPublicKeyRing ring : rings)
        for (PGPPublicKey key : ring)
          if (key.isEncryptionKey())
            return key;

      throw new IllegalStateException("No encryption key found");
    }
  }

  public static PGPPrivateKey extractPrivateKey(PGPSecretKeyRingCollection secrets, char[] passphrase) throws Exception {
    for (PGPSecretKeyRing ring : secrets)
      for (PGPSecretKey key : ring)
        if (key.isSigningKey())
          return key.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(passphrase));

    throw new IllegalStateException("No private key found");
  }

  public static byte[] encrypt(byte[] data, PGPPublicKey key) throws Exception {

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    PGPEncryptedDataGenerator encGen =
        new PGPEncryptedDataGenerator(
            new JcePGPDataEncryptorBuilder(
                PGPEncryptedData.AES_256)
                .setWithIntegrityPacket(true)
                .setSecureRandom(new SecureRandom())
                .setProvider("BC")
        );

    encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(key).setProvider("BC"));

    try (OutputStream encOut = encGen.open(out, data.length)) {
      encOut.write(data);
    }

    return out.toByteArray();
  }

  public static byte[] decrypt(byte[] encrypted, PGPPrivateKey privateKey) throws Exception {
    InputStream in = new ByteArrayInputStream(encrypted);
    in = PGPUtil.getDecoderStream(in);

    PGPObjectFactory factory = new PGPObjectFactory(in, new JcaKeyFingerprintCalculator());

    Object o = factory.nextObject();
    PGPEncryptedDataList list =
        (o instanceof PGPEncryptedDataList)
            ? (PGPEncryptedDataList) o
            : (PGPEncryptedDataList) factory.nextObject();

    PGPPublicKeyEncryptedData data = (PGPPublicKeyEncryptedData) list.get(0);

    InputStream clear = data.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(privateKey));

    return clear.readAllBytes();
  }
}