import json
import os
import random
import time as time0
import urllib.request
from pykafka import KafkaClient

# 设置代理
agents = [
    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1",
    "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/19.77.34.5 Safari/537.1",
    "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/535.24 (KHTML, like Gecko) Chrome/19.0.1055.1 Safari/535.24"
]


def product_reviews(product_id=None, maxPage=None, score=None, ipLocal=None, portLocal=None):
    p = 10
    # 创建生产者
    print(ipLocal + ":" + portLocal)
    client = KafkaClient(hosts=ipLocal + ":" + portLocal)  # 实例化
    print(client.topics)
    print(client.brokers)
    topic = client.topics['test1']
    producer = topic.get_producer()
    while p < maxPage:
        url = 'https://club.jd.com/comment/productPageComments.action?callback=fetchJSON_comment98&productId' \
              '={}&score={}&sortType=6&page={}&pageSize={}&isShadowSku=0&fold=1 '
        url = url.format(product_id, score, p, maxPage)
        cookie = ''
        headers = {
            'User-Agent': ''.join(random.sample(agents, 1)),
            'Referer': 'https://item.jd.com/100018902008.html',
            'Cookie': cookie
        }
        request = urllib.request.Request(url=url, headers=headers)
        time0.sleep(2.5)
        try:
            content = urllib.request.urlopen(request).read().decode('gbk')
        except:
            print('第%d页评论代码出错' % p)
            # p = p + 1
            continue
        # 去掉多余得到json格式
        content = content.strip('fetchJSON_comment98vv995();')

        try:
            obj = json.loads(content)
        except:
            print('信号不好，再次尝试！')
            print([content])
            print(url)
            continue

        comments = obj['comments']
        if len(comments) > 0:
            for comment in comments:
                creationTime = comment['creationTime']
                print(creationTime)
                producer.produce(bytes(str(creationTime), encoding='utf-8'))

            print('data update')
            # print('%s-page---finish(%s/%s)' % (p + 1, p + 1, maxPage))
        else:
            return []
        if p > 0:
            p = p - 1

    producer.stop()


if __name__ == '__main__':
    maxPage = 1000  # 页数
    score = 0  # 评分
    ipLocal = ''  # kafka ip
    portLocal = ''  # kafka 端口
    phone_id =   # 产品id
    product_reviews(product_id=phone_id, maxPage=maxPage, score=score, ipLocal=ipLocal, portLocal=portLocal)
