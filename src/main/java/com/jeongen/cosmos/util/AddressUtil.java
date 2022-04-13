package com.jeongen.cosmos.util;

import com.jeongen.cosmos.crypro.CosmosCredentials;
import io.cosmos.crypto.Crypto;
import io.netty.util.internal.StringUtil;
import org.bitcoinj.core.Bech32;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddressUtil {

    private static final String AddressHRP = "cosmos";

    public static String generatePrivateKey() {
        String priv = Crypto.generatePrivateKey();
        byte[] pub = Crypto.generatePubKeyFromPriv(priv);
        System.out.println("priv");
        System.out.println(priv);
        System.out.println("pubkey");
        System.out.println(Hex.toHexString(pub));
        return priv;
        //priv
        //d7c0e347f2941542012d528bc501cdddbc7204470d208c57a1e9b22901d5b9a8
        //pubkey
        //02f924b088beb5dfca112af402dc3d012520f829fb92e29eef74825badcbebe2cf
//cosmos12pyk5w4kuzh0kqyal4vlwy25hs9ejs230de592

//        priv
//        51bf6cc0780cbdcd0d9cf2cddc67601f846d08e4c895134de3470c9098e50e0b
//        pubkey
//        03689aee52ed8f134db33f45f39c4f9b3fee6f6a0720e56690bff61271753f0312
        //cosmos1ywqs77fx7q4pcnjmn8arkmwscdmgu03etfxkqt
    }

    public static String getAddress(String priv) {
        byte[] pub = Crypto.generatePubKeyFromPriv(priv);
        try {
            String addr = io.cosmos.util.AddressUtil.createNewAddressSecp256k1(AddressHRP, pub);
            return addr;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String publicKeyToAddress(byte[] publicKey) {
        byte[] pubKeyHash = Utils.sha256hash160(publicKey);
        return convertAndEncode(pubKeyHash);
    }

    public static String ecKeyToAddress(ECKey ecKey) {
        byte[] pubKeyHash = ecKey.getPubKeyHash();
        return convertAndEncode(pubKeyHash);
    }

    private static String convertAndEncode(byte[] pubKeyHash) {
        byte[] convertBits = AddressUtil.convertBits(pubKeyHash, 8, 5, true);
        return Bech32.encode(AddressHRP, convertBits);
    }

    public static boolean verifyCosmosAddress(String address) {
        if (address == null || address.length() != 45) {
            return false;
        }
        Bech32.Bech32Data decodeData = null;
        try {
            decodeData = Bech32.decode(address);
        } catch (Exception e) {
            return false;
        }
        if (!AddressHRP.equals(decodeData.hrp)) {
            return false;
        }
        if (decodeData.data.length != 32) {
            return false;
        }
        return true;
    }

    public static CosmosCredentials getCredentials(String mnemonic, String password, String derivePath) {
        if (StringUtil.isNullOrEmpty(derivePath)) {
            return null;
        }

        String[] mnemonicArr = mnemonic.split(" ");

        DeterministicSeed deterministicSeed = new DeterministicSeed(Arrays.asList(mnemonicArr), null, password, 0);
        DeterministicKeyChain deterministicKeyChain = DeterministicKeyChain.builder().seed(deterministicSeed).build();

        List<ChildNumber> childNumbers = decodePath(derivePath);
        DeterministicKey deterministicKey = deterministicKeyChain.getKeyByPath(childNumbers, true);

        return CosmosCredentials.create(deterministicKey);
    }

    public static List<ChildNumber> decodePath(String path) {
        final int HIGHEST_BIT = 0x80000000;

        String[] splitPath = path.split("/");

        int start = 0;
        if (splitPath[0].equals("m")) {
            start = 1;
        }

        int[] result = new int[splitPath.length - start];
        for (int i = start; i < splitPath.length; i++) {
            String splitPathItem = splitPath[i];
            if (splitPathItem.endsWith("'")) {
                splitPathItem = splitPathItem.substring(0, splitPathItem.length() - 1);
                result[i - start] = Integer.parseInt(splitPathItem) + HIGHEST_BIT;
            } else {
                result[i - start] = Integer.parseInt(splitPathItem);
            }
        }

        List<ChildNumber> arr = new ArrayList<>();
        for (int j : result) {
            arr.add(new ChildNumber(j));
        }
        return arr;
    }

    private static byte[] convertBits(byte[] data, int fromBits, int toBits, boolean pad) {
        int acc = 0;
        int bits = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int maxv = (1 << toBits) - 1;
        for (int i = 0; i < data.length; i++) {
            int value = data[i] & 0xff;
            if ((value >>> fromBits) != 0) {
                throw new RuntimeException("ERR_BAD_FORMAT invalid data range: data[" + i + "]=" + value + " (fromBits=" + fromBits + ")");
            }
            acc = (acc << fromBits) | value;
            bits += fromBits;
            while (bits >= toBits) {
                bits -= toBits;
                baos.write((acc >>> bits) & maxv);
            }
        }
        if (pad) {
            if (bits > 0) {
                baos.write((acc << (toBits - bits)) & maxv);
            }
        } else if (bits >= fromBits) {
            throw new RuntimeException("ERR_BAD_FORMAT illegal zero padding");
        } else if (((acc << (toBits - bits)) & maxv) != 0) {
            throw new RuntimeException("ERR_BAD_FORMAT non-zero padding");
        }
        return baos.toByteArray();
    }
}
