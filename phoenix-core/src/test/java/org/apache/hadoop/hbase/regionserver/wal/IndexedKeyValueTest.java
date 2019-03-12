/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.regionserver.wal;

import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.phoenix.hbase.index.wal.IndexedKeyValue;
import org.apache.phoenix.hbase.index.wal.KeyValueCodec;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;


public class IndexedKeyValueTest {

    @Test
    public void testIndexedKeyValuePopulatesKVFields() throws Exception {
        byte[] row = Bytes.toBytes("foo");
        byte[] tableNameBytes = Bytes.toBytes("MyTableName");
        Mutation mutation = new Put(row);
        IndexedKeyValue indexedKeyValue = new IndexedKeyValue(tableNameBytes, mutation);
        testIndexedKeyValueHelper(indexedKeyValue, row, tableNameBytes, mutation);

        //now serialize the IndexedKeyValue and make sure the deserialized copy also
        //has all the right fields
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        KeyValueCodec.write(out, indexedKeyValue);

        IndexedKeyValue deSerializedKV = (IndexedKeyValue)
            KeyValueCodec.readKeyValue(new DataInputStream(
                new ByteArrayInputStream(baos.toByteArray())));
        testIndexedKeyValueHelper(deSerializedKV, row, tableNameBytes, mutation);

    }

    private void testIndexedKeyValueHelper(IndexedKeyValue indexedKeyValue, byte[] row, byte[] tableNameBytes, Mutation mutation) {
        Assert.assertArrayEquals(row, indexedKeyValue.getRowArray());
        Assert.assertEquals(0, indexedKeyValue.getRowOffset());
        Assert.assertEquals(row.length, indexedKeyValue.getRowLength());
        Assert.assertArrayEquals(tableNameBytes, indexedKeyValue.getIndexTable());
        Assert.assertEquals(mutation.toString(), indexedKeyValue.getMutation().toString());
        Assert.assertArrayEquals(WALEdit.METAFAMILY, indexedKeyValue.getFamilyArray());
    }

}
