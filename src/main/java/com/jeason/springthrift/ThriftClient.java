package com.jeason.springthrift;

import com.jeason.springthrift.exception.ThriftClientException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * @Auther: jeason
 * @Date: 2018/8/4 22:48
 * @Description:
 */
public class ThriftClient {
    private String serverHost;
    private int serverPort;

    public ThriftClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public Response request(String serviceName, String operation, String params) {
        String exceptionMsg;
        if (serviceName == null || operation == null) {
            if (serviceName == null) {
                exceptionMsg = "serviceName cannot be null";
            } else {
                exceptionMsg = "operation cannot be null";
            }
            try {
                throw new ThriftClientException(exceptionMsg);
            } catch (ThriftClientException e) {
                e.printStackTrace();
            }
        }

        TTransport transport = new TSocket(serverHost, serverPort);
        TProtocol protocol = new TBinaryProtocol(transport);
        BaseService.Client client = new BaseService.Client(protocol);
        try {
            transport.open();
            return client.request(serviceName, new Request(operation, params));
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } finally {
            transport.close();
        }

        return null;
    }
}
