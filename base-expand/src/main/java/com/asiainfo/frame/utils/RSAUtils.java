package com.asiainfo.frame.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Ares
 * @date: 2018/12/13 18:49
 * @description: RSA工具类 1.生成公钥私钥文件
 * @version: JDK 1.8
 */
public class RSAUtils
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RSAUtils.class);

    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "MD5withRSA";
    private static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String PRIVATE_KEY = "RSAPrivateKey";
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;


    /**
     * @author: Ares
     * @description: 获取公钥字符串
     * @date: 2019/6/13 20:43
     * @Param: [keyMap] 请求参数
     * @return: java.lang.String 响应参数
     */
    public static String getPublicKeyStr(Map<String, Object> keyMap) throws Exception
    {
        // 获得map中的公钥对象 转为key对象
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        // 编码返回字符串
        return encryptBASE64(key.getEncoded());
    }

    /**
     * @author: Ares
     * @description: 获得私钥字符串
     * @date: 2019/6/13 20:44
     * @Param: [keyMap] 请求参数
     * @return: java.lang.String 响应参数
     */
    public static String getPrivateKeyStr(Map<String, Object> keyMap) throws Exception
    {
        // 获得map中的私钥对象 转为key对象
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        // 编码返回字符串
        return encryptBASE64(key.getEncoded());
    }

    /**
     * @author: Ares
     * @description: 获取公钥
     * @date: 2019/6/13 20:44
     * @Param: [key] 请求参数
     * @return: java.security.PublicKey 响应参数
     */
    public static PublicKey getPublicKey(String key) throws Exception
    {
        byte[] keyBytes;
        keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    /**
     * @author: Ares
     * @description: 获取私钥
     * @date: 2019/6/13 20:44
     * @Param: [key] 请求参数
     * @return: java.security.PrivateKey 响应参数
     */
    public static PrivateKey getPrivateKey(String key) throws Exception
    {
        byte[] keyBytes;
        keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    /**
     * @author: Ares
     * @description: 解码返回byte
     * @date: 2019/6/13 20:44
     * @Param: [key] 请求参数
     * @return: byte[] 响应参数
     */
    public static byte[] decryptBASE64(String key) throws Exception
    {
        return (new BASE64Decoder()).decodeBuffer(key);
    }


    /**
     * @author: Ares
     * @description: 编码返回字符串
     * @date: 2019/6/13 20:44
     * @Param: [key] 请求参数
     * @return: java.lang.String 响应参数
     */
    public static String encryptBASE64(byte[] key) throws Exception
    {
        return (new BASE64Encoder()).encodeBuffer(key);
    }

    /**
     * @author: Ares
     * @description: 签名
     * @date: 2019/6/13 20:45
     * @Param: [data, privateKeyStr] 请求参数
     * @return: byte[] 响应参数
     */
    public static byte[] sign(byte[] data, String privateKeyStr) throws Exception
    {
        PrivateKey privateKey = getPrivateKey(privateKeyStr);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    /**
     * @author: Ares
     * @description: 验证
     * @date: 2019/6/13 20:45
     * @Param: [data, sign, publicKeyStr] 请求参数
     * @return: boolean 响应参数
     */
    public static boolean verify(byte[] data, byte[] sign, String publicKeyStr) throws Exception
    {
        PublicKey publicKey = getPublicKey(publicKeyStr);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(sign);
    }

    /**
     * @author: Ares
     * @description: RSA加密解密
     * @date: 2019/6/13 20:45
     * @Param: [cipher, input] 请求参数
     * @return: byte[] 响应参数
     */
    public static byte[] rasCrypt(Cipher cipher, byte[] input) throws BadPaddingException, IllegalBlockSizeException, IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int inputLen = input.length;
        int offSet = 0;
        byte[] cache;
        int i = 0;
        while(inputLen - offSet > 0)
        {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK)
            {
                cache = cipher.doFinal(input, offSet, MAX_ENCRYPT_BLOCK);
            }
            else
            {
                cache = cipher.doFinal(input, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] output = out.toByteArray();
        out.close();
        return output;
    }

    /**
     * @author: Ares
     * @description: 加密
     * @date: 2019/6/13 20:45
     * @Param: [plainText, publicKeyStr] 请求参数
     * @return: byte[] 响应参数
     */
    public static byte[] encrypt(byte[] plainText, String publicKeyStr) throws Exception
    {
        PublicKey publicKey = getPublicKey(publicKeyStr);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return rasCrypt(cipher, plainText);
    }

    /**
     * @author: Ares
     * @description: 解密
     * @date: 2019/6/13 20:45
     * @Param: [encryptText, privateKeyStr] 请求参数
     * @return: byte[] 响应参数
     */
    public static byte[] decrypt(byte[] encryptText, String privateKeyStr) throws Exception
    {
        PrivateKey privateKey = getPrivateKey(privateKeyStr);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return rasCrypt(cipher, encryptText);
    }

    public static Map<String, Object> initKey() throws Exception
    {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);

        keyPairGen.initialize(1024);

        KeyPair keyPair = keyPairGen.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        Map<String, Object> keyMap = new HashMap<String, Object>(2);

        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);

        return keyMap;
    }

    /**
     * @author: Ares
     * @description: 生成公钥和私钥文件
     * @date: 2019/6/13 20:45
     * @Param: [publicKeyPath, privateKeyPath]
     * 公钥文件路径,私钥文件路径
     * @return: void 响应参数
     */
    public static void buildPublicAndPrivateKey(String publicKeyPath, String privateKeyPath)
    {
        Map<String, Object> keyMap;
        BufferedWriter writer = null;
        try
        {
            keyMap = initKey();

            String publicKey = getPublicKeyStr(keyMap);
            String privateKey = getPrivateKeyStr(keyMap);

            File file = new File(publicKeyPath);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            writer.write(publicKey);
            writer.close();

            file = new File(privateKeyPath);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            writer.write(privateKey);
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            IOHandleUtil.closeIOSteam(writer);
        }
    }

    /**
     * @author: Ares
     * @description: 根据公钥文件加密
     * @date: 2019/6/13 20:46
     * @Param: [input, publicKeyPath] 请求参数
     * @return: byte[] 响应参数
     */
    public static byte[] encryptByPublicKeyFile(String input, String publicKeyPath)
    {
        //密文byte
        byte[] cipherText = null;
        BufferedReader reader = null;
        try
        {
            StringBuilder publicKey = new StringBuilder();
            File file = new File(publicKeyPath);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String tempStr;
            while((tempStr = reader.readLine()) != null)
            {
                publicKey.append(tempStr);
            }

            cipherText = encrypt(input.getBytes(), publicKey.toString());
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            IOHandleUtil.closeIOSteam(reader);
        }
        return cipherText;
    }

    /**
     * @author: Ares
     * @description: 利用秘钥文件解密
     * @date: 2019/6/13 20:46
     * @Param: [encryptData, privateKeyPath] 请求参数
     * @return: java.lang.String 响应参数
     */
    public static String decryptByPrivateKeyFile(byte[] encryptData, String privateKeyPath)
    {
        BufferedReader reader = null;
        String decryptData = null;
        try
        {
            StringBuilder privateKey = new StringBuilder();
            File file = new File(privateKeyPath);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String tempStr;
            while((tempStr = reader.readLine()) != null)
            {
                privateKey.append(tempStr);
            }
            reader.close();

            byte[] plainText = decrypt(encryptData, privateKey.toString());
            decryptData = new String(plainText);
            LOGGER.info("解密后明文: {}", decryptData);
        } catch (Exception e)
        {
            LOGGER.error("解密出错: ", e);
        } finally
        {
            IOHandleUtil.closeIOSteam(reader);
        }
        return decryptData;
    }
}
