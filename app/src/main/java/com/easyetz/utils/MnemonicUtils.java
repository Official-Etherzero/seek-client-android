package com.easyetz.utils;

import android.content.res.AssetManager;

import com.easyetz.app.MyApp;
import com.easyetz.blockchain.walletutils.WalletUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import static org.web3j.compat.Compat.UTF_8;
import static org.web3j.crypto.Hash.sha256;

/**
 * Provides utility methods to generate random mnemonics and also generate
 * seeds from mnemonics.
 *
 * @see <a href="https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki">Mnemonic code
 * for generating deterministic keys</a>
 */
public class MnemonicUtils {

    /**
     * Create entropy from the mnemonic.
     *
     * @param mnemonic The input mnemonic which should be 128-160 bits in length containing
     *                 only valid words
     * @return Byte array representation of the entropy
     */
    public static byte[] generateEntropy(String mnemonic) {
        final BitSet bits = new BitSet();
        final int size = mnemonicToBits(mnemonic, bits);
        if (size == 0) {
            throw new IllegalArgumentException("Empty mnemonic");
        }

        final int ent = 32 * size / 33;
        if (ent % 8 != 0) {
            throw new IllegalArgumentException("Wrong mnemonic size");
        }
        final byte[] entropy = new byte[ent / 8];
        for (int i = 0; i < entropy.length; i++) {
            entropy[i] = readByte(bits, i);
        }
        validateEntropy(entropy);

        final byte expectedChecksum = calculateChecksum(entropy);
        final byte actualChecksum = readByte(bits, entropy.length);
        if (expectedChecksum != actualChecksum) {
            throw new IllegalArgumentException("Wrong checksum");
        }

        return entropy;
    }

    public static List<String> getWords(String mnemonic) {
        return Collections.unmodifiableList(populateWordList(mnemonic));
    }

    public static boolean validateMnemonic(String mnemonic) {
        try {
            generateEntropy(mnemonic);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    private static void validateEntropy(byte[] entropy) {
        if (entropy == null) {
            throw new IllegalArgumentException("Entropy is required");
        }

        int ent = entropy.length * 8;
        if (ent < 128 || ent > 256 || ent % 32 != 0) {
            throw new IllegalArgumentException("The allowed size of ENT is 128-256 bits of "
                    + "multiples of 32");
        }
    }

    private static int mnemonicToBits(String mnemonic, BitSet bits) {
        int bit = 0;
        final List<String> vocabulary = getWords(mnemonic);
        final StringTokenizer tokenizer = new StringTokenizer(mnemonic, " ");
        while (tokenizer.hasMoreTokens()) {
            final String word = tokenizer.nextToken();
            final int index = vocabulary.indexOf(word);
            if (index < 0) {
                throw new IllegalArgumentException(String.format(
                        "Mnemonic word '%s' should be in the word list", word));
            }
            for (int k = 0; k < 11; k++) {
                bits.set(bit++, isBitSet(index, 10 - k));
            }
        }
        return bit;
    }

    private static byte readByte(BitSet bits, int startByte) {
        byte res = 0;
        for (int k = 0; k < 8; k++) {
            if (bits.get(startByte * 8 + k)) {
                res = (byte) (res | (1 << (7 - k)));
            }
        }
        return res;
    }

    private static boolean isBitSet(int n, int k) {
        return ((n >> k) & 1) == 1;
    }

    public static byte calculateChecksum(byte[] initialEntropy) {
        int ent = initialEntropy.length * 8;
        byte mask = (byte) (0xff << 8 - ent / 32);
        byte[] bytes = sha256(initialEntropy);

        return (byte) (bytes[0] & mask);
    }

    private static List<String> populateWordList(String mnemonic) {
        String[] LANGS = {"zh", "en", "es", "fr", "ja", "zhTW"};
        String[] mnemonucs = mnemonic.split(" ");
        List<String> words = null;
        AssetManager assetManager = MyApp.getmInstance().getResources().getAssets();
        try {
            for (String code : LANGS) {
                int index = 0;
                String fileName = WalletUtils.getWordFileName(code);

                InputStream inputStream = assetManager.open(fileName);
                words = readAllLines(inputStream);
                for (String m : mnemonucs) {
                    if (!words.contains(m)) break;
                    index++;
                }
                if (index == 12) break;


            }
            if (words == null) {
                InputStream input = assetManager.open(WalletUtils.getWordFileName("en"));
                words = readAllLines(input);
            }
            return words;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
//    private static List<String> populateWordList(String mnemonic) {
//
//       String fileName= WalletUtils.getWordFileName("zh");
//        AssetManager assetManager = MyApp.getmInstance().getResources().getAssets();
//        try {
//            InputStream inputStream = assetManager.open(fileName);
//            return readAllLines(inputStream);
//        } catch (Exception e) {
//            throw new IllegalStateException(e);
//        }
//    }

    private static List<String> readAllLines(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        List<String> data = new ArrayList<>();
        for (String line; (line = br.readLine()) != null; ) {
            data.add(line);
        }
        return data;
    }


}

