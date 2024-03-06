package cn.wxl475.filter;

import cn.wxl475.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoginCheckFilter implements GlobalFilter, Ordered {

    @Value("${jwt.signKey}")
    private String signKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain filterChain) {
        ServerHttpRequest req=  exchange.getRequest();
        ServerHttpResponse resp = exchange.getResponse();

        //1.获取url
        String url = req.getURI().getPath();
        log.info("请求的url:{}",url);

        //2.判断url是否是被放行
        if(req.getMethod().toString().equals("POST")){
            if(url.equals("/user/login")){
                log.info("登录操作直接放行");
                return filterChain.filter(exchange);
            }
            if (url.equals("/goods/getGoodsById") || url.equals("/goods/getGoodsByConditions") || url.equals("/goods/getTodayPrice")|| url.equals("/goods/getGoodsByGoodsType")){
                log.info("获取商品基本信息操作，放行");
                return filterChain.filter(exchange);
            }
        }

        //3.获取请求头令牌(token)
        String jwt = req.getHeaders().getFirst("Authorization");

        //4.判断令牌是否存在，不存在返回错误结果，未登录
        if(!StringUtils.hasLength(jwt)){
            log.info("请求头token为空，返回未登录信息");
            resp.setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
//       5.解析令牌，如果失败，返回错误结果
        try {
            Claims claims = JwtUtils.parseJWT(jwt, signKey);
        } catch (Exception e) {//解析失败
            log.info("解析令牌失败，返回未登录错误信息");
            resp.setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
//        6.放行
        log.info("持有合法令牌，放行");
        return filterChain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
