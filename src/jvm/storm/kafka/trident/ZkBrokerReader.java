package storm.kafka.trident;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.kafka.DynamicBrokersReader;
import storm.kafka.ZkHosts;

import java.util.Map;


public class ZkBrokerReader implements IBrokerReader {

    public static final Logger LOG = LoggerFactory.getLogger(ZkBrokerReader.class);

    GlobalPartitionInformation cachedBrokers;
    DynamicBrokersReader reader;
    long lastRefreshTimeMs;


    long refreshMillis;

    public ZkBrokerReader(Map conf, String topic, ZkHosts hosts) {
    	//DynamicBrokerReader��������BrokerReader��������zk�л�ȡtopic��partition��broker�����Ͷ˿�
        reader = new DynamicBrokersReader(conf, hosts.brokerZkStr, hosts.brokerZkPath, topic);
        //��ȡ��partition��broker��ӳ��
        cachedBrokers = reader.getBrokerInfo();
        lastRefreshTimeMs = System.currentTimeMillis();
        //ZkHost��д���Ĺ̶�60��
        refreshMillis = hosts.refreshFreqSecs * 1000L;

    }

    //����partition��broker��ӳ�䣬ֻ�����г�ʱ��zkˢ�£����û��ʱ����zk����Ϣ�Ѿ��ͻ���Ĳ�ͬ�ˣ�
    @Override
    public GlobalPartitionInformation getCurrentBrokers() {
        long currTime = System.currentTimeMillis();
        if (currTime > lastRefreshTimeMs + refreshMillis) {
            LOG.info("brokers need refreshing because " + refreshMillis + "ms have expired");
            cachedBrokers = reader.getBrokerInfo();
            lastRefreshTimeMs = currTime;
        }
        return cachedBrokers;
    }

    @Override
    public void close() {
        reader.close();
    }
}
