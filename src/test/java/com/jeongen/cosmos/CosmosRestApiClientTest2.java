package com.jeongen.cosmos;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.jeongen.cosmos.crypro.CosmosCredentials;
import com.jeongen.cosmos.vo.SendInfo;
import cosmos.auth.v1beta1.QueryOuterClass;
import cosmos.bank.v1beta1.Tx;
import cosmos.base.abci.v1beta1.Abci;
import cosmos.base.tendermint.v1beta1.Query;
import cosmos.base.v1beta1.CoinOuterClass;
import cosmos.tx.v1beta1.ServiceOuterClass;
import cosmos.tx.v1beta1.TxOuterClass;
import io.cosmos.msg.MsgSend;
import junit.framework.TestCase;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import tendermint.types.Types;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CosmosRestApiClientTest2 extends TestCase {
    public static final String BaseUrl = "http://8.214.27.159:1317"; // "http://8.214.27.159:1317";

    //https://api.cosmos.network
    public static void main(String[] args) throws Exception {
        //testSendMultiTx();
    }

    /**
     * //priv
     * //d7c0e347f2941542012d528bc501cdddbc7204470d208c57a1e9b22901d5b9a8
     * //pubkey
     * //02f924b088beb5dfca112af402dc3d012520f829fb92e29eef74825badcbebe2cf
     * //cosmos12pyk5w4kuzh0kqyal4vlwy25hs9ejs230de592
     * <p>
     * //        priv
     * //        51bf6cc0780cbdcd0d9cf2cddc67601f846d08e4c895134de3470c9098e50e0b
     * //        pubkey
     * //        03689aee52ed8f134db33f45f39c4f9b3fee6f6a0720e56690bff61271753f0312
     * //cosmos1ywqs77fx7q4pcnjmn8arkmwscdmgu03etfxkqt
     *
     * @throws Exception
     */
    public void testSendMultiTx() throws Exception {
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient(BaseUrl, "cosmoshub-4", "uatom");
        // 私钥生成公钥、地址
        byte[] privateKey = Hex.decode("d7c0e347f2941542012d528bc501cdddbc7204470d208c57a1e9b22901d5b9a8");
        CosmosCredentials credentials = CosmosCredentials.create(privateKey);
        // 获取地址
        System.out.println("address:" + credentials.getAddress());
        List<SendInfo> sendList = new ArrayList<>();
        sendList.add(SendInfo.builder().credentials(credentials).toAddress("cosmos1ywqs77fx7q4pcnjmn8arkmwscdmgu03etfxkqt").amountInAtom(new BigDecimal("0.0001")).build());
        TxOuterClass.Tx tx = cosmosRestApiClient.getTxRequest(credentials, sendList, new BigDecimal("0.0002"), 90000);
        System.out.println("tx===" + tx);
        Abci.TxResponse txResponse = cosmosRestApiClient.broadcastTx(tx);
        System.out.println("txResponse===" + txResponse);
        // 生成、签名、广播交易
//        Abci.TxResponse txResponse = cosmosRestApiClient.sendMultiTx(credentials, sendList, new BigDecimal("0.000001"), 200000);
        System.out.println(tx);
    }

    public void testQueryAccount() throws Exception {
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient(BaseUrl, "cosmoshub-4", "uatom");
        // 获取指定高度的交易
        QueryOuterClass.QueryAccountResponse res = cosmosRestApiClient.queryAccount("cosmos12pyk5w4kuzh0kqyal4vlwy25hs9ejs230de592");

        System.out.println(res);
    }

    public void testGetTx() throws Exception {
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient(BaseUrl, "cosmoshub-4", "uatom");
        // 获取指定高度的交易
        ServiceOuterClass.GetTxResponse tx = cosmosRestApiClient.getTx("115FE7D6C9DDEB7423BEB808ADAB669DD67FF850D16728E413CBB08B5D92A9C4");
        System.out.println("----");
        List<Any> messagesList = tx.getTx().getBody().getMessagesList();
        for (Any messages : messagesList) {
            String typeUrl = messages.getTypeUrl();
            if (typeUrl.equalsIgnoreCase("/cosmos.bank.v1beta1.MsgSend")) {
                Tx.MsgSend msgSend = messages.unpack(Tx.MsgSend.class);
                String fromAddress = msgSend.getFromAddress();
                String toAddress = msgSend.getToAddress();
                List<CoinOuterClass.Coin> amountList = msgSend.getAmountList();
                for (CoinOuterClass.Coin coin : amountList) {
                    String denom = coin.getDenom();
                    String amount = coin.getAmount();
                    System.out.println("fromAddress=" + fromAddress + "  toAddress=" + toAddress + " amount=" + amount + denom);
                }

            }
        }

        System.out.println(tx);
    }


    public void testHeight() throws Exception {
        List<Tx.MsgSend> msgSendList = new ArrayList<>();
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient(BaseUrl, "cosmoshub-4", "uatom");
        // 获取指定高度的交易
        Query.GetBlockByHeightResponse blockByHeight = cosmosRestApiClient.getBlockByHeight(10099441L);
        Types.Data data = blockByHeight.getBlock().getData();

        List<ByteString> txsList = data.getTxsList();

        for (ByteString bytes : txsList) {
            TxOuterClass.TxRaw txRaw = TxOuterClass.TxRaw.parseFrom(bytes);
            ByteString bodyBytes = txRaw.getBodyBytes();
            try {
                Any any = Any.parseFrom(bodyBytes);
                String txStr = any.getTypeUrl();
               // System.out.println(any.getTypeUrl()  +" --- " + any.getValue());
                if (txStr.contains("cosmos.bank.v1beta1.MsgSend")) {
                    System.out.println(txStr);
                    String[] split = txStr.split("\\n");
                    String[] addressStr = split[2].split("-");
                    String amountStr = split[3].trim();

                    String fromAddress = addressStr[1].trim();
                    String toAddress = addressStr[2].trim().replaceAll(" \\f", "");

                    Long amount  = getLongValue(amountStr);
                    String denom = amountStr.replace(amount+"","").trim();
                    Tx.MsgSend msgSend = Tx.MsgSend.newBuilder().setFromAddress(fromAddress).setToAddress(toAddress).addAmount(CoinOuterClass.Coin.newBuilder().setAmount(amount + "").setDenom(denom).build()).build();

                    System.out.println("fromAddress=" + fromAddress + "toAddress=" + toAddress + "amount=" + amount + denom);
                    msgSendList.add(msgSend);
                }
            }catch (Exception ex){

            }

        }

    }

    public void testHeightLog() throws Exception {
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient(BaseUrl, "cosmoshub-4", "uatom");

        ServiceOuterClass.GetTxsEventResponse txsEventByHeight = cosmosRestApiClient.getTxsEventByHeight(10099441L, "");
        System.out.println(txsEventByHeight.getTxsList());
        List<TxOuterClass.Tx> txsList = txsEventByHeight.getTxsList();

        for (TxOuterClass.Tx tx : txsList) {
            TxOuterClass.TxBody body = tx.getBody();
            List<Any> messagesList = body.getMessagesList();
            for (Any any : messagesList) {
                if (any.getTypeUrl().contains("cosmos.bank.v1beta1.MsgSend")) {
                    Tx.MsgSend msgSend = any.unpack(Tx.MsgSend.class);
                    String fromAddress = msgSend.getFromAddress();
                    String toAddress = msgSend.getToAddress();
                    List<CoinOuterClass.Coin> amountList = msgSend.getAmountList();
                    for (CoinOuterClass.Coin coin : amountList) {
                        String denom = coin.getDenom();
                        String amount = coin.getAmount();
                        System.out.println("fromAddress=" + fromAddress + "  toAddress=" + toAddress + " amount=" + amount + denom);

                    }
                }
            }

        }
    }

    public void testHeightLog2() throws Exception {
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient(BaseUrl, "cosmoshub-4", "uatom");

        ServiceOuterClass.GetTxsEventResponse txsEventByHeight = cosmosRestApiClient.getTxsEventByHeight(10099441L, "");
//        System.out.println(txsEventByHeight.getTxsList());
        System.out.println(txsEventByHeight.getTxResponsesList());
        List<Abci.TxResponse> txResponsesList = txsEventByHeight.getTxResponsesList();

        for (Abci.TxResponse tx : txResponsesList) {
            int code = tx.getCode();
            String txhash = tx.getTxhash();
            List<Tx.MsgSend> msgSendList=new ArrayList<>();
            Any any = tx.getTx();
            TxOuterClass.Tx unpack = any.unpack(TxOuterClass.Tx.class);
            List<Any> messagesList = unpack.getBody().getMessagesList();
            for (Any any1 : messagesList) {
                if (any1.getTypeUrl().contains("cosmos.bank.v1beta1.MsgSend")) {
                    Tx.MsgSend msgSend = any1.unpack(Tx.MsgSend.class);
                    String fromAddress = msgSend.getFromAddress();
                    String toAddress = msgSend.getToAddress();
                    List<CoinOuterClass.Coin> amountList = msgSend.getAmountList();
                    for (CoinOuterClass.Coin coin : amountList) {
                        String denom = coin.getDenom();
                        String amount = coin.getAmount();
                        System.out.println("fromAddress=" + fromAddress + "  toAddress=" + toAddress + " amount=" + amount + denom);

                    }

                    msgSendList.add(msgSend);
                }
            }

        }
    }

    public void testSendMultiTxBak() throws Exception {
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient("https://api.cosmos.network", "cosmoshub-4", "uatom");
        // 私钥生成公钥、地址
        byte[] privateKey = Hex.decode("c2ad7a31c06ea8bb560a0467898ef844523f2f804dec96fedf65906dbb951f24");
        CosmosCredentials credentials = CosmosCredentials.create(privateKey);
        // 获取地址
        System.out.println("address:" + credentials.getAddress());
        List<SendInfo> sendList = new ArrayList<>();
        sendList.add(SendInfo.builder().credentials(credentials).toAddress("cosmos12kd7gu4lamw29pv4u6ug8aryr0p7wm207uwt30").amountInAtom(new BigDecimal("0.0001")).build());
        sendList.add(SendInfo.builder().credentials(credentials).toAddress("cosmos1u3zluamfx5pvgha0dn73ah4pyu9ckv6scvdw72").amountInAtom(new BigDecimal("0.0001")).build());
        // 生成、签名、广播交易
//        Abci.TxResponse txResponse = cosmosRestApiClient.sendMultiTx(credentials, sendList, new BigDecimal("0.000001"), 200000);
//        System.out.println(txResponse);
//
        // 获取指定高度的交易
        ServiceOuterClass.GetTxsEventResponse txsEventByHeight = cosmosRestApiClient.getTxsEventByHeight(10091547L, "");
        System.out.println(txsEventByHeight);
    }



    /**
     * 解析str，获得其中的整数
     * @param str 待解析的str
     */
    public static Long getLongValue(String str) {
        Long r = 0L;
        if (str != null && str.length() != 0) {
            StringBuffer bf = new StringBuffer();

            char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c >= '0' && c <= '9') {
                    bf.append(c);
                } else if (c == ',') {
                    continue;
                } else {
                    if (bf.length() != 0) {
                        break;
                    }
                }
            }
            try {
                r = Long.parseLong(bf.toString());
            } catch (Exception e) {
            }
        }
        return r;
    }
}