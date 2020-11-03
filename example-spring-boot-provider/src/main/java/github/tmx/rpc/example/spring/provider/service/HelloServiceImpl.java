package github.tmx.rpc.example.spring.provider.service;

import github.tmx.example.api.Hello;
import github.tmx.example.api.HelloService;
import github.tmx.rpc.core.spring.annotion.RpcService;

/**
 * @author: TangMinXuan
 */
@RpcService
public class HelloServiceImpl implements HelloService {

    @Override
    public Hello sayHello(Hello hello){
        System.out.println("hello from client: " + "id: " + hello.getId() + " " + "message: " + hello.getMessage());
        Hello helloReturn = new Hello(hello.getId() + 1, "Hello, client");
        return helloReturn;
    }
}
