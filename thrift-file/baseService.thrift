namespace java com.mohuoer.thrift
namespace py mohuoer.thrift

struct Request{
	1: string operation,
	2: string params
}

struct Response{
	1: string data,
	2: i32 code,
	3: string message
}

service BaseService{
	Response request(string serviceName, Request request)
}