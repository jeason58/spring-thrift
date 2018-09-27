package com.jeason.springthrift;

import com.alibaba.fastjson.JSONObject;
import com.jeason.springthrift.exception.ThriftServerException;
import com.jeason.springthrift.handler.ThriftServiceHandler;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Auther: jeason
 * @Date: 2018/8/4 00:08
 * @Description: thrift server
 */
public class ThriftServer {

  private static class ServiceProcessor implements BaseService.Iface {

    private static Logger log = LoggerFactory.getLogger(ServiceProcessor.class);
    private Map<String, ThriftServiceHandler> serviceHandlerMap = new HashMap<>();

    public void registerService(String serviceName, ThriftServiceHandler serviceHandler) {
      serviceHandlerMap.put(Objects.requireNonNull(serviceName), Objects.requireNonNull(serviceHandler));
      log.info("registered serviceHandler: {}", serviceName);
    }

    @Override
    public Response request(String serviceName, Request request) throws TException {
      log.info("receive request | [serviceName: {}, request: {}]", serviceName, request);
      Response response = null;
      if (serviceName == null) {
        response = new Response(null, 400, "serviceName is null!");
      } else if (request == null) {
        response = new Response(null, 400, "request is null!");
      } else {
        ThriftServiceHandler serviceHandler = serviceHandlerMap.get(serviceName);
        String operation = request.getOperation();
        String params = request.getParams();
        JSONObject paramJson = JSONObject.parseObject(params);
        response = serviceHandler.handle(operation, paramJson);
      }
      log.info(
          "return response | [code: {}, message: {}, data: {}]",
          response.getCode(), response.getMessage(), response.getData()
      );

      return response;
    }
  }

  /**
   * static fields
   */
  private static Logger logger = LoggerFactory.getLogger(ThriftServer.class);
  private static final int DEFAULT_SERVER_PORT = 9527;
  private static final String DEFAULT_SERVER_NAME = "SpringThrift-Server";
  private static final String SERVICE_NAME_SEPARATOR = ",";
  private volatile static ThriftServer serverInstance = null;

  /**
   * instant fields
   */
  private int port;
  private String serverName;
  private String serviceNames;
  // service处理器，必须实现BaseService.Iface接口，用于初始化TProcessor
  private ServiceProcessor serviceProcessor;

  /**
   * constructor
   */
  private ThriftServer(int port, String serverName, String serviceNames) {
    this.port = port == 0 ? DEFAULT_SERVER_PORT : port;
    this.serverName = serverName == null ? DEFAULT_SERVER_NAME : serverName;
    this.serviceNames = serviceNames;
    this.serviceProcessor = new ServiceProcessor();
  }

  /**
   * @param context a instance of spring's ApplicationContext, such as the result of
   * SpringApplication.run(Class class)
   * @desc initialize serviceProcessor, private function for constructor calling
   */
  private static void initServiceProcessor(ApplicationContext context) {
    String serviceNames = null;
    String[] serviceNameArr = null;
    if ((serviceNames = serverInstance.serviceNames) != null) {
      if (serviceNames.contains(SERVICE_NAME_SEPARATOR)) {
        serviceNameArr = serviceNames.split(SERVICE_NAME_SEPARATOR);
      } else {
        serviceNameArr = new String[]{serviceNames};
      }
      for (String serviceName : serviceNameArr) {
        ThriftServiceHandler serviceHandler = (ThriftServiceHandler) context.getBean(serviceName);
        serverInstance.serviceProcessor.registerService(serviceName, serviceHandler);
      }
    }
  }

  /**
   * getInstance of ThriftServer
   */
  public static ThriftServer newInstance(ApplicationContext context)
      throws ThriftServerException {
    // DCL for singleton instance
    if (serverInstance == null) {
      synchronized (ThriftServer.class) {
        if (serverInstance == null) {
          Environment env = context.getEnvironment();
          String portStr = env.getProperty("springthrift.server.port");
          // springthrift.server.port is required, or it'll be the default value with 9527
          int port = (portStr == null ? DEFAULT_SERVER_PORT : Integer.valueOf(portStr));
          // springthrift.server.name is optional parameter
          String serverName = env.getProperty("springthrift.server.name");
          // springthrift.server.handlerNames is required,
          // it is your Service which implements the ThriftServiceHandler interface
          // please separate your handlerNames with "," when handlerNames' number more than 1
          String serviceNames = env.getProperty("springthrift.server.handlerNames");
          serverInstance = new ThriftServer(port, serverName, serviceNames);
          initServiceProcessor(context);
        }
      }
    }

    return serverInstance;
  }

  /**
   * @desc start the server
   */
  public void startServer() {
    try {
      TServerSocket serverTransport = new TServerSocket(port);
      TProcessor processor = new BaseService.Processor<>(serviceProcessor);
      TBinaryProtocol.Factory protocolFactory = new TBinaryProtocol.Factory();
      TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);
      args.processor(processor);
      args.protocolFactory(protocolFactory);
      TServer server = new TThreadPoolServer(args);
      logger.info(serverName + " started on port " + port + "...");
      server.serve();
      logger.info(serverName + " is shutdown!");
    } catch (TTransportException e) {
      e.printStackTrace();
    }
  }
}
