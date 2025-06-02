package com.market.main.emarket.config;


import org.apache.hadoop.hbase.thrift.generated.Hbase;
import org.apache.hadoop.hbase.thrift.generated.TRowResult;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class HBaseThriftClient {

    private final String host;
    private final int port;

    @Autowired
    public HBaseThriftClient(
            @Value("${hbase.thrift.host}") String host,
            @Value("${hbase.thrift.port}") int port
    ) {
        this.host = host;
        this.port = port;
    }

    public List<Long> getUserRecommendation(String rowKeyStr) throws Exception {
        TTransport transport = new TSocket(host, port);
        transport.open();

        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        Hbase.Client client = new Hbase.Client(protocol);

        ByteBuffer table = ByteBuffer.wrap("user_recommendations".getBytes());
        ByteBuffer rowKey = ByteBuffer.wrap(rowKeyStr.getBytes());

        List<TRowResult> results = client.getRow(table, rowKey, null);
        Map<Long, Double> output = new HashMap<>();

        for (TRowResult row : results) {
            row.columns.forEach((col, cell) -> {
                String column = new String(col.array());
                String value = new String(cell.value.array());
                output.put(Long.valueOf(column.replace("cf:","")),Double.valueOf(value));
            });
               }
        Map<Long,Double> sortedMap = output.entrySet().stream()
                        .sorted(Map.Entry.<Long,Double>comparingByValue().reversed())
                .limit(10)
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (e1,e2)->e1,
                                        LinkedHashMap::new
                                ));
        List<Long> top10=sortedMap.keySet().stream().collect(Collectors.toList());
        transport.close();
        return top10;
    }
}

