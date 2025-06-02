package com.market.main.emarket.config;


import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.hadoop.hbase.thrift.generated.TRowResult;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.nio.ByteBuffer;
import java.util.*;

public class HBaseThriftClient {

    private final String host;
    private final int port;

    public HBaseThriftClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Map<String, String> getUserRecommendation(String rowKeyStr) throws Exception {
        TTransport transport = new TSocket(host, port);
        transport.open();

        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        Hbase.Client client = new Hbase.Client(protocol);

        ByteBuffer table = ByteBuffer.wrap("user_recommendations".getBytes());
        ByteBuffer rowKey = ByteBuffer.wrap(rowKeyStr.getBytes());

        List<TRowResult> results = client.getRow(table, rowKey, null);
        Map<String, String> output = new HashMap<>();

        for (TRowResult row : results) {
            row.columns.forEach((col, cell) -> {
                String column = new String(col.array());
                String value = new String(cell.value.array());
                output.put(column, value);
            });
        }

        transport.close();
        return output;
    }
}

