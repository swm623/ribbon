/*
 *
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.netflix.niws.client.http;

import java.net.URI;

import org.junit.Test;

import com.netflix.client.ClientFactory;
import com.netflix.client.http.HttpRequest;
import com.netflix.config.ConfigurationManager;
import com.netflix.loadbalancer.AbstractLoadBalancer;
import com.netflix.loadbalancer.WeightedResponseTimeRule;

public class ResponseTimeWeightedRuleTest {
    
    @Test
    public void testServerWeights(){
        try{
            ConfigurationManager.loadPropertiesFromResources("sample-client.properties"); 

            ConfigurationManager.getConfigInstance().setProperty(
                    "sample-client.ribbon.NFLoadBalancerClassName", "com.netflix.loadbalancer.DynamicServerListLoadBalancer");
            ConfigurationManager.getConfigInstance().setProperty(
                    "sample-client.ribbon.NFLoadBalancerRuleClassName", "com.netflix.loadbalancer.WeightedResponseTimeRule");
            // shorter weight adjusting interval
            ConfigurationManager.getConfigInstance().setProperty(
                    "sample-client.ribbon." + WeightedResponseTimeRule.WEIGHT_TASK_TIMER_INTERVAL_CONFIG_KEY, "5000");
            ConfigurationManager.getConfigInstance().setProperty(
                    "sample-client.ribbon.InitializeNFLoadBalancer", "true");       

            RestClient client = (RestClient) ClientFactory.getNamedClient("sample-client"); 

            HttpRequest request = HttpRequest.newBuilder().uri(new URI("/")).build(); 

            for (int i = 0; i < 20; i++) {
                client.executeWithLoadBalancer(request);
            }
            System.out.println(((AbstractLoadBalancer) client.getLoadBalancer()).getLoadBalancerStats());
            // wait for the weights to be adjusted
            Thread.sleep(5000);
            for (int i = 0; i < 50; i++) {
                client.executeWithLoadBalancer(request);
            }
            System.out.println(((AbstractLoadBalancer) client.getLoadBalancer()).getLoadBalancerStats());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
