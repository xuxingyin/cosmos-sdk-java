package com.jeongen.cosmos;

import com.jeongen.cosmos.crypro.CosmosCredentials;
import com.jeongen.cosmos.vo.SendInfo;
import cosmos.auth.v1beta1.QueryOuterClass;
import cosmos.base.abci.v1beta1.Abci;
import cosmos.base.tendermint.v1beta1.Query;
import cosmos.tx.v1beta1.ServiceOuterClass;
import cosmos.tx.v1beta1.TxOuterClass;
import junit.framework.TestCase;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CosmosRestApiClientTest2 extends TestCase  {
    public static final String BaseUrl ="https://api.cosmos.network"; // "http://8.214.27.159:1317";
    //https://api.cosmos.network
    public static void main(String[] args) throws Exception {
        //testSendMultiTx();
       // getBlockByHeight();
        balence();
    }

    /**
     *  //priv
     *         //d7c0e347f2941542012d528bc501cdddbc7204470d208c57a1e9b22901d5b9a8
     *         //pubkey
     *         //02f924b088beb5dfca112af402dc3d012520f829fb92e29eef74825badcbebe2cf
     * //cosmos12pyk5w4kuzh0kqyal4vlwy25hs9ejs230de592
     *
     * //        priv
     * //        51bf6cc0780cbdcd0d9cf2cddc67601f846d08e4c895134de3470c9098e50e0b
     * //        pubkey
     * //        03689aee52ed8f134db33f45f39c4f9b3fee6f6a0720e56690bff61271753f0312
     *         //cosmos1ywqs77fx7q4pcnjmn8arkmwscdmgu03etfxkqt
     * @throws Exception
     */
    public  void testSendMultiTx() throws Exception {
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient(BaseUrl, "cosmoshub-4", "uatom");
        // 私钥生成公钥、地址
        byte[] privateKey = Hex.decode("51bf6cc0780cbdcd0d9cf2cddc67601f846d08e4c895134de3470c9098e50e0b");
        CosmosCredentials credentials = CosmosCredentials.create(privateKey);
        // 获取地址
        System.out.println("address:" + credentials.getAddress());
        List<SendInfo> sendList = new ArrayList<>();
        sendList.add(SendInfo.builder().credentials(credentials).toAddress("cosmos12pyk5w4kuzh0kqyal4vlwy25hs9ejs230de592").amountInAtom(new BigDecimal("0.0001")).build());
        TxOuterClass.Tx tx = cosmosRestApiClient.getTxRequest(credentials, sendList,  new BigDecimal("0.000001"), 90000);
        System.out.println("tx==="+tx);
        //广播交易  得到交易hashaddress:cosmos1ywqs77fx7q4pcnjmn8arkmwscdmgu03etfxkqt
        //tx===body {
        //  messages {
        //    type_url: "/cosmos.bank.v1beta1.MsgSend"
        //    value: "\n-cosmos1ywqs77fx7q4pcnjmn8arkmwscdmgu03etfxkqt\022-cosmos12pyk5w4kuzh0kqyal4vlwy25hs9ejs230de592\032\f\n\005uatom\022\003100"
        //  }
        //}
        //auth_info {
        //  signer_infos {
        //    public_key {
        //      type_url: "/cosmos.crypto.secp256k1.PubKey"
        //      value: "\n!\003h\232\356R\355\217\023M\263?E\363\234O\233?\356oj\a \345f\220\277\366\022qu?\003\022"
        //    }
        //    mode_info {
        //      single {
        //        mode: SIGN_MODE_DIRECT
        //      }
        //    }
        //    sequence: 1
        //  }
        //  fee {
        //    amount {
        //      denom: "uatom"
        //      amount: "1"
        //    }
        //    gas_limit: 90000
        //  }
        //}
        //signatures: "E\354\235\002m\246\254[\350D\a\255z\031\241\005\205\222\004;Jo\266y\037YF\n\001\245\230\223u\\A\361\350\321o\213\316^\223\261W\034\254\371E\254\265\347\263%?\355\302\377\334{f\277\317\213"
        Abci.TxResponse txResponse = cosmosRestApiClient.broadcastTx(tx);
        System.out.println("txResponse==="+txResponse);
        // 生成、签名、广播交易
//        Abci.TxResponse txResponse = cosmosRestApiClient.sendMultiTx(credentials, sendList, new BigDecimal("0.000001"), 200000);
        System.out.println(tx);
    }

    public  void testQueryAccount() throws Exception {
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient(BaseUrl, "cosmoshub-4", "uatom");
        // 获取指定高度的交易
        QueryOuterClass.QueryAccountResponse  res = cosmosRestApiClient.queryAccount("cosmos12pyk5w4kuzh0kqyal4vlwy25hs9ejs230de592");

        System.out.println(res);
    }
    public  void testGetTx() throws Exception {
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient(BaseUrl, "cosmoshub-4", "uatom");
        // 获取指定高度的交易
        //ServiceOuterClass.GetTxResponse tx = cosmosRestApiClient.getTx("F62CFE77AA631F8B0B37FA1E77E3FA689725327BFC46EC42C84AFFBBBE496457");
        ServiceOuterClass.GetTxResponse tx = cosmosRestApiClient.getTx("115FE7D6C9DDEB7423BEB808ADAB669DD67FF850D16728E413CBB08B5D92A9C4");
        System.out.println(tx);
    }


    public  void testHeight() throws Exception {
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient(BaseUrl, "cosmoshub-4", "uatom");
        // 获取指定高度的交易
        ServiceOuterClass.GetTxsEventResponse txsEventByHeight = cosmosRestApiClient.getTxsEventByHeight(10091547L, "");
        System.out.println(txsEventByHeight);
    }

    public  void testSendMultiTxBak() throws Exception {
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


    public static void balence() throws Exception {
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient(BaseUrl, "cosmoshub-4", "");
        // 获取余额
       BigDecimal account = cosmosRestApiClient.getBalanceInAtomAll( "cosmos12pyk5w4kuzh0kqyal4vlwy25hs9ejs230de592");
        System.out.println(account);
    }

    @Test
    public static void getBlockByHeight() throws Exception {
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient(BaseUrl, "cosmoshub-4", "");
        // 拉块
        Query.GetBlockByHeightResponse account = cosmosRestApiClient.getBlockByHeight(10099441L);

        System.out.println(account);

    }

}