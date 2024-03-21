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
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.springframework.util.Assert;

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
        ASN1ObjectIdentifier pkOid = X9ObjectIdentifiers.id_ecPublicKey;
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
     * 通过反射bc包中的 {@link org.bouncycastle.crypto.signers.SM2Signer SM2Signer} 的私有方法实现 <br>
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

    /**
     * 签名值或签名结构，转128位
     * @param ssv 签名值或签名结构
     * @return 128签名值
     */
    public static byte[] signValueTo128(byte[] ssv) {
        Assert.notNull(ssv, "签名为空");
        if (ssv.length == 64) {
            // 旧版本签名值为64位，非DER编码
            return signValue64To128(ssv);
        }
        try {
            ASN1Sequence asn1Sequence = (ASN1Sequence) ASN1Sequence.fromByteArray(ssv);
            ASN1Integer ra = (ASN1Integer) asn1Sequence.getObjectAt(0);
            ASN1Integer sa = (ASN1Integer) asn1Sequence.getObjectAt(1);
            byte[] r = encodeIntegerTo32Byte(ra.getEncoded());
            byte[] s = encodeIntegerTo32Byte(sa.getEncoded());
            return signValue64To128(ByteUtils.concatenate(r, s));
        } catch (Exception e) {
            log.info("签名值转换失败");
            throw new RuntimeException("签名值转换失败," + e.getMessage());
        }
    }

    /**
     * ASN1Integer转32位byte
     * @param s ASN1Integer
     * @return byte32
     */
    private static byte[] encodeIntegerTo32Byte(byte[] s) {
        if (s[0] != 2 || s[1] > 33) {
            log.info("签名值长度错误");
            throw new RuntimeException("签名值长度错误");
        }
        int length = s[1];
        if (length == 33) {
            if (s[2] != 0) {
                throw new RuntimeException("签名值长度错误");
            }
            byte[] tem = new byte[32];
            System.arraycopy(s, 3, tem, 0, 32);
            return tem;
        } else {
            byte[] tem = new byte[32];
            System.arraycopy(s, 2, tem, 32 - length, length);
            return tem;
        }
    }

    /**
     * 64位签名值转换为128
     *
     * @param signData 需要转换的签名值
     * @return 128位签名值
     */
    public static byte[] signValue64To128(byte[] signData) {
        if (signData != null && signData.length == 64) {
            //logger.info("将64的签名值转换成128:"+Base64EnOrDe.encode(signData));
            byte[] sign_pre = new byte[128];
            System.arraycopy(signData, 0, sign_pre, 32, 32);
            System.arraycopy(signData, 32, sign_pre, 96, 32);
            return sign_pre;
        }
        return signData;
    }
}
