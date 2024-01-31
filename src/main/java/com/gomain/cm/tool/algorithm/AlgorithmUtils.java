package com.gomain.cm.tool.algorithm;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author caimeng
 * @date 2024/1/26 16:26
 */
@Slf4j
public class AlgorithmUtils {
    /**
     * 将132的公钥变成65位
     *
     * @param publicKey 公钥
     * @return 65位公钥值
     */
    public static byte[] publicKey132to65(byte[] publicKey) {
        //如果公钥长度不是132是65，则需要对公钥进行编码
        if (publicKey.length == 132) {
            byte[] pk = new byte[65];
            //首位需要补充04
            pk[0] = 0x04;
            System.arraycopy(publicKey, 36, pk, 1, 32);
            System.arraycopy(publicKey, 100, pk, 33, 32);
            return pk;
        }
        return publicKey;
    }

    /***
     * 将128位的签名值转换成证书里编码后的签名值
     * @param src 需要转换的128位签名值
     * @return 转换后的签名值
     */
    public static byte[] getSM2128ToDerSignValue(byte[] src) {
        if (src.length != 128){
            log.error("{} [128]: src.length == {}", "签名值长度错误", src.length);
            throw new RuntimeException("签名值长度错误");
        }
        //第一段签名值
        byte[] r = new byte[32];
        //第二段签名值
        byte[] s = new byte[32];
        System.arraycopy(src, 32, r, 0, 32);
        System.arraycopy(src, 96, s, 0, 32);
        // 强制整数
        BigInteger bigInteger = new BigInteger(1, r);
        BigInteger bigInteger1 = new BigInteger(1, s);
        if (bigInteger.toByteArray().length < 32 || bigInteger1.toByteArray().length < 32) {
            log.error("{} [32]: i1 = {}, i2 = {}",
                    "签名值长度错误",
                    bigInteger.toByteArray().length,
                    bigInteger1.toByteArray().length);
            throw new RuntimeException("签名值长度错误");
        }
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(bigInteger));
        v.add(new ASN1Integer(bigInteger1));
        try {
            return new DERSequence(v).getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            throw new RuntimeException("编码ASN1失败");
        }
    }



    /**
     * 公钥转公钥结构
     * @param pk 公钥
     * @return 公钥结构
     */
    @SneakyThrows
    public static byte[] pkToDer(byte[] pk) {
        // id-ecPublicKey
        ASN1ObjectIdentifier pkOid = new ASN1ObjectIdentifier("1.2.840.10045.2.1");
        // SM2
        ASN1ObjectIdentifier sm2Oid = GMObjectIdentifiers.sm2p256v1;
        // 公钥头
        ASN1EncodableVector schemeAsn1 = new ASN1EncodableVector();
        schemeAsn1.add(pkOid);
        schemeAsn1.add(sm2Oid);
        DERSequence schemeDer = new DERSequence(schemeAsn1);
        // 公钥结构
        ASN1EncodableVector pkAsn1 = new ASN1EncodableVector();
        pkAsn1.add(schemeDer);
        pkAsn1.add(new DERBitString(pk));
        DERSequence pkDer = new DERSequence(pkAsn1);
        return pkDer.getEncoded();
    }

    /**
     * 生成公钥对象
     * @param pkDer 公钥结构
     * @return 公钥对象
     */
    @SneakyThrows
    public static PublicKey pkToPublicKey(byte[] pkDer) {
        final BouncyCastleProvider bc = new BouncyCastleProvider();
        KeyFactory keyFact = KeyFactory.getInstance("EC", bc);
        return keyFact.generatePublic(new X509EncodedKeySpec(pkDer));
    }

    /**
     * 从公钥结构中解析公钥值
     * @param pkDer 公钥结构
     * @return 公钥值
     */
    @SneakyThrows
    public static byte[] der2Pk(byte[] pkDer) {
        ASN1Sequence derSequence = (ASN1Sequence) DERSequence.fromByteArray(pkDer);
        DERBitString bitString = (DERBitString) derSequence.getObjectAt(1);
        return bitString.getOctets();
    }

    /**
     * 带公钥的摘要 <br>
     * 使用公钥对待摘要数据进行预处理  <br>
     * 通过反射bc包中的 {@link org.bouncycastle.crypto.signers SM2Signer} 的私有方法实现 <br>
     * @param pkDer 公钥结构
     * @param dataHash 待摘要数据
     * @return 公钥预处理后的摘要值
     */
    @SneakyThrows
    public static byte[] sm3digestWithPubKeyPre(byte[] pkDer, byte[] dataHash) {
        SM2Signer sm2Signer = new SM2Signer();
        PublicKey publicKey = AlgorithmUtils.pkToPublicKey(pkDer);
        ECPublicKeyParameters parameters = (ECPublicKeyParameters) ECUtil
                .generatePublicKeyParameter(publicKey);
        sm2Signer.init(false, parameters);
        sm2Signer.update(dataHash, 0, dataHash.length);
        // 私有方法调用
        Method method = SM2Signer.class.getDeclaredMethod("digestDoFinal");
        method.setAccessible(true);
        return  (byte[]) method.invoke(sm2Signer);
    }
}
