package com.jeongen.cosmos;

import com.jeongen.cosmos.crypro.CosmosCredentials;
import com.jeongen.cosmos.vo.SendInfo;
import cosmos.base.abci.v1beta1.Abci;
import cosmos.tx.v1beta1.ServiceOuterClass;
import junit.framework.TestCase;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CosmosRestApiClientDev extends TestCase  {
    public static final String BaseUrl ="https://api.cosmos.network"; // "http://8.214.27.159:1317";
    //https://api.cosmos.network
    public static void main(String[] args) throws Exception {
        //testSendMultiTx();
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
        byte[] privateKey = Hex.decode("d7c0e347f2941542012d528bc501cdddbc7204470d208c57a1e9b22901d5b9a8");
        CosmosCredentials credentials = CosmosCredentials.create(privateKey);
        // 获取地址
        System.out.println("address:" + credentials.getAddress());
        List<SendInfo> sendList = new ArrayList<>();
        sendList.add(SendInfo.builder().credentials(credentials).toAddress("cosmos1ywqs77fx7q4pcnjmn8arkmwscdmgu03etfxkqt").amountInAtom(new BigDecimal("0.0001")).build());
        // 生成、签名、广播交易
        Abci.TxResponse txResponse = cosmosRestApiClient.sendMultiTx(credentials, sendList, new BigDecimal("0.000001"), 200000);
        System.out.println(txResponse);
    }

    public  void testHeight() throws Exception {
        CosmosRestApiClient cosmosRestApiClient = new CosmosRestApiClient(BaseUrl, "cosmoshub-4", "uatom");
        // 获取指定高度的交易
        ServiceOuterClass.GetTxsEventResponse txsEventByHeight = cosmosRestApiClient.getTxsEventByHeight(10091547L, "");
        System.out.println(txsEventByHeight);
    }
}