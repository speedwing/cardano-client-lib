package com.bloxbean.cardano.client.cip.cip8;

import co.nstant.in.cbor.model.*;
import com.bloxbean.cardano.client.exception.CborRuntimeException;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.bloxbean.cardano.client.cip.cip8.COSEUtil.decodeIntOrTextTypeFromDataItem;
import static com.bloxbean.cardano.client.cip.cip8.COSEUtil.getDataItemFromIntOrTextObject;

@Accessors(fluent = true)
@Data
public class COSEKey implements COSEItem {
    //INT(1) key type
    private Object keyType;
    //INT(2)
    private byte[] keyId;
    //INT(3) algorithm identifier. (EdDSA, ChaChaPoly, etc)
    private Object algorithmId;
    //INT(4) operations that this key is valid for if this field exists
    private List<Object> keyOps = new ArrayList<>();
    //INT(5)
    private byte[] baseInitVector;

    //all other headers not listed above
    private LinkedHashMap<Object, DataItem> otherHeaders = new LinkedHashMap<>();

    public static COSEKey deserialize(DataItem dataItem) {
        if (!MajorType.MAP.equals(dataItem.getMajorType())) {
            throw new CborRuntimeException("De-serialization error. Expected type: Map, Found : %s" + dataItem.getMajorType());
        }

        Map map = (Map) dataItem;
        COSEKey coseKey = new COSEKey();

        Collection<DataItem> keyDIs = map.getKeys();

        for (DataItem keyDI : keyDIs) {
            DataItem valueDI = map.get(keyDI);

            if (keyDI.equals(new UnsignedInteger(1))) {
                if (valueDI != null)
                    coseKey.keyType = decodeIntOrTextTypeFromDataItem(valueDI);
            } else if (keyDI.equals(new UnsignedInteger(2))) {
                if (valueDI != null)
                    coseKey.keyId = ((ByteString) valueDI).getBytes();
            } else if (keyDI.equals(new UnsignedInteger(3))) {
                if (valueDI != null)
                    coseKey.algorithmId = decodeIntOrTextTypeFromDataItem(valueDI);
            } else if (keyDI.equals(new UnsignedInteger(4))) {
                if (valueDI != null && valueDI.getMajorType().equals(MajorType.ARRAY)) {
                    Array keyOpsArray = (Array) valueDI;
                    coseKey.keyOps = keyOpsArray.getDataItems().stream()
                            .map(di -> decodeIntOrTextTypeFromDataItem(di))
                            .collect(Collectors.toList());
                }
            } else if (keyDI.equals(new UnsignedInteger(5))) {
                if (valueDI != null)
                    coseKey.baseInitVector = ((ByteString) valueDI).getBytes();
            } else {
                //other headers
                coseKey.otherHeaders.put(decodeIntOrTextTypeFromDataItem(keyDI), valueDI);
            }
        }

        return coseKey;
    }

    public COSEKey keyType(long keyType) {
        this.keyType = keyType;
        return this;
    }

    public COSEKey keyType(String keyType) {
        this.keyType = keyType;
        return this;
    }

    public COSEKey algorithmId(long algorithmId) {
        this.algorithmId = algorithmId;
        return this;
    }

    public COSEKey algorithmId(String algorithmId) {
        this.algorithmId = algorithmId;
        return this;
    }

    public COSEKey keyOp(long keyOp) {
        this.keyOps.add(keyOp);
        return this;
    }

    public COSEKey keyOp(String keyOp) {
        this.keyOps.add(keyOp);
        return this;
    }

    public COSEKey addOtherHeader(BigInteger key, DataItem value) {
        otherHeaders.put(key, value);
        return this;
    }

    public COSEKey addOtherHeader(long key, DataItem value) {
        otherHeaders.put(key, value);
        return this;
    }

    public COSEKey addOtherHeader(String key, DataItem value) {
        otherHeaders.put(key, value);
        return this;
    }

    @Override
    public Map serialize() {
        Map map = new Map();

        if (keyType != null)
            map.put(new UnsignedInteger(1), getDataItemFromIntOrTextObject(keyType));

        if (keyId != null)
            map.put(new UnsignedInteger(2), new ByteString(keyId));

        if (algorithmId != null)
            map.put(new UnsignedInteger(3), getDataItemFromIntOrTextObject(algorithmId));

        if (keyOps != null && keyOps.size() > 0) {
            Array valueArray = new Array();
            keyOps.stream().forEach(crit -> {
                valueArray.add(getDataItemFromIntOrTextObject(crit));
            });
            map.put(new UnsignedInteger(4), valueArray);
        }

        if (baseInitVector != null)
            map.put(new UnsignedInteger(5), new ByteString(baseInitVector));

        //Other headers
        otherHeaders.forEach((key, value) -> {
            map.put(getDataItemFromIntOrTextObject(key), value);
        });

        return map;
    }
}
