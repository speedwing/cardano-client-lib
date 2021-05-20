package com.bloxbean.cardano.client.backend.api.helper;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.backend.api.BlockService;
import com.bloxbean.cardano.client.backend.api.TransactionService;
import com.bloxbean.cardano.client.backend.api.UtxoService;
import com.bloxbean.cardano.client.backend.exception.ApiException;
import com.bloxbean.cardano.client.backend.impl.blockfrost.common.Constants;
import com.bloxbean.cardano.client.backend.impl.blockfrost.service.BFBaseTest;
import com.bloxbean.cardano.client.backend.impl.blockfrost.service.BFBlockService;
import com.bloxbean.cardano.client.backend.impl.blockfrost.service.BFTransactionService;
import com.bloxbean.cardano.client.backend.impl.blockfrost.service.BFUtxoService;
import com.bloxbean.cardano.client.backend.model.Block;
import com.bloxbean.cardano.client.backend.model.Result;
import com.bloxbean.cardano.client.backend.model.TransactionContent;
import com.bloxbean.cardano.client.backend.model.TransactionDetailsParams;
import com.bloxbean.cardano.client.backend.model.request.PaymentTransaction;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.*;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.TransactionSerializationException;
import com.bloxbean.cardano.client.transaction.model.Asset;
import com.bloxbean.cardano.client.transaction.model.MultiAsset;
import com.bloxbean.cardano.client.transaction.model.script.ScriptPubkey;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.client.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TransactionHelperServiceIT extends BFBaseTest {
    UtxoService utxoService;
    TransactionService transactionService;
    TransactionHelperService transactionHelperService;
    BlockService blockService;

    @BeforeEach
    public void setup() {
        utxoService = new BFUtxoService(Constants.BLOCKFROST_TESTNET_URL, projectId);
        transactionService = new BFTransactionService(Constants.BLOCKFROST_TESTNET_URL, projectId);
        transactionHelperService = new TransactionHelperService(utxoService, transactionService);
        blockService = new BFBlockService(Constants.BLOCKFROST_TESTNET_URL, projectId);
    }

    @Test
    void transfer() throws TransactionSerializationException, CborException, AddressExcepion, ApiException {

        String senderMnemonic = "damp wish scrub sentence vibrant gauge tumble raven game extend winner acid side amused vote edge affair buzz hospital slogan patient drum day vital";
        Account sender = new Account(Networks.testnet(), senderMnemonic);
        String receiver = "addr_test1qqwpl7h3g84mhr36wpetk904p7fchx2vst0z696lxk8ujsjyruqwmlsm344gfux3nsj6njyzj3ppvrqtt36cp9xyydzqzumz82";

        PaymentTransaction paymentTransaction =
                PaymentTransaction.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .amount(BigInteger.valueOf(1500000))
                        .fee(BigInteger.valueOf(230000))
                        .unit("lovelace")
                        .build();

        Result<String> result = transactionHelperService.transfer(paymentTransaction, TransactionDetailsParams.builder().ttl(getTtl()).build());

        if(result.isSuccessful())
            System.out.println("Transaction Id: " + result.getValue());
        else
            System.out.println("Transaction failed: " + result);

        waitForTransaction(result);

        assertThat(result.isSuccessful(), is(true));
    }

    @Test
    void transferMultiAsset() throws TransactionSerializationException, CborException, AddressExcepion, ApiException {
        //Sender address : addr_test1qzx9hu8j4ah3auytk0mwcupd69hpc52t0cw39a65ndrah86djs784u92a3m5w475w3w35tyd6v3qumkze80j8a6h5tuqq5xe8y
        String senderMnemonic = "damp wish scrub sentence vibrant gauge tumble raven game extend winner acid side amused vote edge affair buzz hospital slogan patient drum day vital";
        Account sender = new Account(Networks.testnet(), senderMnemonic);
        String receiver = "addr_test1qqwpl7h3g84mhr36wpetk904p7fchx2vst0z696lxk8ujsjyruqwmlsm344gfux3nsj6njyzj3ppvrqtt36cp9xyydzqzumz82";

        PaymentTransaction paymentTransaction =
                PaymentTransaction.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .amount(BigInteger.valueOf(12))
                        .fee(BigInteger.valueOf(210000))
                        .unit("329728f73683fe04364631c27a7912538c116d802416ca1eaf2d7a96736174636f696e")
                        .build();

        Result<String> result = transactionHelperService.transfer(paymentTransaction, TransactionDetailsParams.builder().ttl(getTtl()).build());

        if(result.isSuccessful())
            System.out.println("Transaction Id: " + result.getValue());
        else
            System.out.println("Transaction failed: " + result);

        System.out.println(result);
        waitForTransaction(result);
        assertThat(result.isSuccessful(), is(true));
    }

    @Test
    void transferMultiAssetMultiPayments() throws TransactionSerializationException, CborException, AddressExcepion, ApiException {
        //Sender address : addr_test1qzx9hu8j4ah3auytk0mwcupd69hpc52t0cw39a65ndrah86djs784u92a3m5w475w3w35tyd6v3qumkze80j8a6h5tuqq5xe8y
        String senderMnemonic = "damp wish scrub sentence vibrant gauge tumble raven game extend winner acid side amused vote edge affair buzz hospital slogan patient drum day vital";
        Account sender = new Account(Networks.testnet(), senderMnemonic);

        //addr_test1qqwpl7h3g84mhr36wpetk904p7fchx2vst0z696lxk8ujsjyruqwmlsm344gfux3nsj6njyzj3ppvrqtt36cp9xyydzqzumz82
        String senderMnemonic2 = "mixture peasant wood unhappy usage hero great elder emotion picnic talent fantasy program clean patch wheel drip disorder bullet cushion bulk infant balance address";
        Account sender2 = new Account(Networks.testnet(), senderMnemonic2);

        String receiver = "addr_test1qqwpl7h3g84mhr36wpetk904p7fchx2vst0z696lxk8ujsjyruqwmlsm344gfux3nsj6njyzj3ppvrqtt36cp9xyydzqzumz82";

        String receiver2 = "addr_test1qz7r5eu2jg0hx470mmf79vpgueaggh22pmayry8xrre5grtpyy9s8u2heru58a4r68wysmdw9v40zznttmwrg0a6v9tq36pjak";

        String receiver3 = "addr_test1qrp6x6aq2m28xhvxhqzufl0ff7x8gmzjejssrk29mx0q829dsty3hzmrl2k8jhwzghgxuzfjatgxlhg9wtl6ecv0v3cqf92rnh";

        PaymentTransaction paymentTransaction1 =
                PaymentTransaction.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .amount(BigInteger.valueOf(14))
                        .fee(BigInteger.valueOf(210000))
                        .unit("329728f73683fe04364631c27a7912538c116d802416ca1eaf2d7a96736174636f696e")
                        .build();

        PaymentTransaction paymentTransaction2 =
                PaymentTransaction.builder()
                        .sender(sender)
                        .receiver(receiver2)
                        .amount(BigInteger.valueOf(33))
                        .fee(BigInteger.valueOf(210000))
                        .unit("329728f73683fe04364631c27a7912538c116d802416ca1eaf2d7a96736174636f696e")
                        .build();

        PaymentTransaction paymentTransaction3 =
                PaymentTransaction.builder()
                        .sender(sender2)
                        .receiver(receiver3)
                        .amount(BigInteger.valueOf(3110000))
                        .fee(BigInteger.valueOf(210000))
                        .unit(LOVELACE)
                        .build();

        Result<String> result = transactionHelperService.transfer(Arrays.asList(paymentTransaction1, paymentTransaction2, paymentTransaction3),
                TransactionDetailsParams.builder().ttl(getTtl()).build());

        if(result.isSuccessful())
            System.out.println("Transaction Id: " + result.getValue());
        else
            System.out.println("Transaction failed: " + result);

        System.out.println(result);
        waitForTransaction(result);
        assertThat(result.isSuccessful(), is(true));
    }

    @Test
    void mintToken() throws TransactionSerializationException, CborException, AddressExcepion, ApiException {

        Keys keys = KeyGenUtil.generateKey();
        VerificationKey vkey = keys.getVkey();
        SecretKey skey = keys.getSkey();

        ScriptPubkey scriptPubkey = ScriptPubkey.create(vkey);
        String policyId = scriptPubkey.getPolicyId();

        String senderMnemonic = "damp wish scrub sentence vibrant gauge tumble raven game extend winner acid side amused vote edge affair buzz hospital slogan patient drum day vital";
        Account sender = new Account(Networks.testnet(), senderMnemonic);
        String receiver = "addr_test1qqwpl7h3g84mhr36wpetk904p7fchx2vst0z696lxk8ujsjyruqwmlsm344gfux3nsj6njyzj3ppvrqtt36cp9xyydzqzumz82";

        MultiAsset multiAsset = new MultiAsset();
        multiAsset.setPolicyId(policyId);
        Asset asset = new Asset(HexUtil.encodeHexString("testtoken".getBytes(StandardCharsets.UTF_8)), BigInteger.valueOf(200000));
        multiAsset.getAssets().add(asset);

        PaymentTransaction paymentTransaction =
                PaymentTransaction.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .fee(BigInteger.valueOf(230000))
                        .mintAssets(Arrays.asList(multiAsset))
                        .build();

        Result<String> result = transactionHelperService.mintToken(paymentTransaction,
                TransactionDetailsParams.builder().ttl(getTtl()).build(), scriptPubkey, skey);

        if(result.isSuccessful())
            System.out.println("Transaction Id: " + result.getValue());
        else
            System.out.println("Transaction failed: " + result);

        waitForTransaction(result);

        assertThat(result.isSuccessful(), is(true));
    }

    private Integer getTtl() throws ApiException {
        Block block = blockService.getLastestBlock().getValue();
        Integer slot = block.getSlot();
        return slot + 2000;
    }

    private void waitForTransaction(Result<String> result) {
        try {
            if (result.isSuccessful()) { //Wait for transaction to be mined
                int count = 0;
                while (count < 20) {
                    Result<TransactionContent> txnResult = transactionService.getTransaction(result.getValue());
                    if (txnResult.isSuccessful()) {
                        System.out.println(JsonUtil.getPrettyJson(txnResult.getValue()));
                        break;
                    } else {
                        System.out.println("Waiting for training to be mined ....");
                    }

                    count++;
                    Thread.sleep(2000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
