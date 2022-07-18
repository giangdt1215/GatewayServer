package com.optimagrowth.gateway.filters;

import brave.Span;
import brave.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class ResponseFilter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseFilter.class);

    @Autowired
    private Tracer tracer;

    @Autowired
    private FilterUtils filterUtils;

    @Bean
    public GlobalFilter postGlobalFilter(){
        return (exchange, chain) -> {
            return chain.filter(exchange).
                    then(Mono.fromRunnable(() -> {
                        //HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
                        //String correlationId = filterUtils.getCorrelationId(requestHeaders);
                        Span span = tracer.currentSpan();
                        if(span != null) {
                            String traceId = span.context().traceIdString();
                            logger.debug("Adding the correlation id to the outbound headers. {}", traceId);
                            exchange.getResponse().getHeaders().add(FilterUtils.CORRELATION_ID, traceId);
                        }

                        logger.debug("Completing outgoing request for {}", exchange.getRequest().getURI());
                    }));
        };
    }
}
