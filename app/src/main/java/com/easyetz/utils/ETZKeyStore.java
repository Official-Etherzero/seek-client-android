package com.easyetz.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import com.easyetz.app.MyApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.security.auth.x500.X500Principal;


public class ETZKeyStore {
    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    public static final String NEW_CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    public static final String NEW_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE;
    public static final String NEW_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM;
    public static final String PHRASE_ALIAS = "phrase";
    private static final String PHRASE_IV = "ivphrase";
    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";

    public static final int AUTH_DURATION_SEC = 300;
    private static final ReentrantLock lock = new ReentrantLock();

    public static String encryptData(String data) {
        String encrypt = "";
        KeyStore keyStore = null;
        try {
            lock.lock();
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            Cipher inCipher;

//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            inCipher = Cipher.getInstance(RSA_MODE);
            PublicKey publicKey = null;
            if (keyStore.getCertificate(PHRASE_ALIAS) != null) {
                publicKey = keyStore.getCertificate(PHRASE_ALIAS).getPublicKey();
            }
            if (publicKey == null) {
                createKeys_BelowApi23(PHRASE_ALIAS);
                publicKey = keyStore.getCertificate(PHRASE_ALIAS).getPublicKey();
            }
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);
//            }
//            else {
//                inCipher = Cipher.getInstance(NEW_CIPHER_ALGORITHM);
//                SecretKey secretKey = (SecretKey) keyStore.getKey(PHRASE_ALIAS, null);
//                if (secretKey == null) {
//                    //create key if not present
//                    secretKey = createKeys(PHRASE_ALIAS, false);
//                    inCipher.init(Cipher.ENCRYPT_MODE, secretKey);
//                } else {
//                    //see if the key is old format, create a new one if it is
//                    inCipher.init(Cipher.ENCRYPT_MODE, secretKey);
//
//                }
//                byte[] iv = inCipher.getIV();
//               SharedPrefsUitls.getInstance(). storeEncryptedData(iv, PHRASE_IV);
//            }

            byte[] encryptedData = inCipher.doFinal(data.getBytes());
            MyLog.i("encryptedData=" + new String(encryptedData));
            MyLog.i("encryptedData=" + Base64.encodeToString(encryptedData, Base64.DEFAULT));
//            //store the encrypted data
            return Base64.encodeToString(encryptedData, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            lock.unlock();
        }

    }

    @SuppressLint("NewApi")
    public synchronized static String decodetData(String data) {
        KeyStore keyStore = null;

//        MyLog.e( "_getData: " + alias);
        try {
            lock.lock();
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            SecretKey secretKey = null;
            PrivateKey privateKey = null;
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            privateKey = (PrivateKey) keyStore.getKey(PHRASE_ALIAS, null);
//            } else {
//                secretKey = (SecretKey) keyStore.getKey(PHRASE_ALIAS, null);
//            }

            byte[] encryptedData = Base64.decode(data, Base64.DEFAULT);
            if (encryptedData != null) {
//
                Cipher outCipher;

//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                outCipher = Cipher.getInstance(RSA_MODE);
                outCipher.init(Cipher.DECRYPT_MODE, privateKey);
//                } else {
//                new format data is present, good
//                byte[] iv = SharedPrefsUitls.getInstance().retrieveEncryptedData(PHRASE_IV);
//                if (iv == null) {
//                    return null;
//                }
//                    outCipher = Cipher.getInstance(NEW_CIPHER_ALGORITHM);
//                    outCipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
////                    outCipher.init(Cipher.DECRYPT_MODE, secretKey);
//                }

                try {
                    byte[] decryptedData = outCipher.doFinal(encryptedData);
                    if (decryptedData != null) {
                        return new String(decryptedData);
                    }
                } catch (IllegalBlockSizeException | BadPaddingException e) {
                    e.printStackTrace();
                    throw new RuntimeException("failed to decrypt data: " + e.toString());
                }
            }
            //no new format data, get the old one and migrate it to the new format


        } catch (InvalidKeyException e) {
            if (e instanceof UserNotAuthenticatedException) {
                /** user not authenticated, ask the system for authentication */
            } else {
                MyLog.e("InvalidKeyException" + e);
                if (e instanceof KeyPermanentlyInvalidatedException)
                    e.printStackTrace();
            }
        } catch (IOException | CertificateException | KeyStoreException e) {
            /** keyStore.load(null) threw the Exception, meaning the keystore is unavailable */
            MyLog.e("Exception:" + e);
            if (e instanceof FileNotFoundException) {
                MyLog.e("FileNotFoundException:" + e);

                RuntimeException ex = new RuntimeException("the key is present but the phrase on the disk no");
                throw new RuntimeException(e.getMessage());
            } else {
                throw new RuntimeException(e.getMessage());
            }

        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            /** if for any other reason the keystore fails, crash! */
            MyLog.e("getData: error: " + e.getClass().getSuperclass().getName());
            throw new RuntimeException(e.getMessage());
        } finally {
            lock.unlock();
        }
        return "";

    }

    private static void createKeys_BelowApi23(String alias) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 30);
        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(MyApp.getmInstance())
                .setAlias(alias)
                .setSubject(new X500Principal("CN=" + alias))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();

        KeyPairGenerator keyPairGenerator = null;
        keyPairGenerator = KeyPairGenerator.getInstance("RSA", ANDROID_KEY_STORE);
        keyPairGenerator.initialize(spec);
        keyPairGenerator.generateKeyPair();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static SecretKey createKeys(String alias, boolean auth_required) throws InvalidAlgorithmParameterException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException {
        KeyGenerator keyGenerator;

        // Set the alias of the entry in Android KeyStore where the key will appear
        // and the constrains (purposes) in the constructor of the Builder

        keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        keyGenerator.init(new KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(NEW_BLOCK_MODE)
                .setUserAuthenticationRequired(auth_required)
                .setUserAuthenticationValidityDurationSeconds(AUTH_DURATION_SEC)
                .setRandomizedEncryptionRequired(false)
                .setEncryptionPaddings(NEW_PADDING)
                .build());
        return keyGenerator.generateKey();

    }
}
