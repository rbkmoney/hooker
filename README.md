# webhooker (hooker)
Сервис вебхуков

[![Build Status](http://ci.rbkmoney.com/buildStatus/icon?job=rbkmoney_private/hooker/master)](http://ci.rbkmoney.com/job/rbkmoney_private/job/hooker/job/master/)

1. Сервис предоставляет интерфейс для CAPI для создания, удаления вебхуков, установки/редактирования опций вебхука (таких как url, public/private keys - они генерируются сервисом, типы событий). 
2. Сервис поллит bustermaze на появление событий, по которым должны отправляться сообщения мерчантам

Интерфейс для capi доступен по пути /hook

Для более подробного ознакомления со структурой объектов можно воспользоваться ссылкой на сам объект [webhooker.thrift][1]

Подробная информация по протоколу отправки сообщений мерчантам описана в [swagger-спецификации][2] 

#### Пример запроса к мерчанту

```
curl -v -X POST 
-H "Content-Type: application/json; charset=utf-8" 
-H "Content-Signature: alg=RS256; digest=QydtAwZ8jmmrX1wP87vAJIXVBiRZe3Zo5xRYnzMQIsDxxsVn26XGMBeD4op8_9LmXIAirYWd53gfX638ZR83D2pTSCXkNpzHyqiyzhPlH2asaC9qiiOOWaQ-kHk9xFlOcMa1Qtt0BaVst-tbGDsFhgjl6tvGuN4JHRq5khzl_iIJ_ZQniEuzGOYReWn8wCLmaspX0MPyACMTo8HDwyihOHB1_RQBliBFYyvw523xvSQ6WxWYjYhFsjglIg1wdKUMyaiScw0kmKm53OdxDAnjl4MPQmtryuANjbklN8_EatOrQAqGwRUp1ayR_3WMlayhpxaEHlG1sAHQaaO3ulI35g==" 
-d '{"eventID":27,"occuredAt":"2017-05-16T13:49:34.935099Z","topic":"InvoicesTopic","eventType":"PaymentCaptured","invoice":{"id":"qXMiygTqb2","shopID":1,"createdAt":"2017-05-16T13:49:32.753723Z","status":"unpaid","reason":null,"dueDate":"2017-05-16T13:59:32Z","amount":100000,"currency":"RUB","metadata":{"type":"application/json","data":"eyJpbnZvaWNlX2R1bW15X2NvbnRleHQiOiJ0ZXN0X3ZhbHVlIn0="},"product":"test_product","description":"test_invoice_description"},"payment":{"id":"1","createdAt":"2017-05-16T13:49:33.182195Z","status":"captured","error":null,"amount":100000,"currency":"RUB","paymentToolToken":"5Gz2nhE1eleFGBAcGe9SrA","paymentSession":"2nTYVgk6h85O7vIVV9j4pA","contactInfo":{"email":"bla@bla.ru","phoneNumber":null},"ip":"10.100.2.1","fingerprint":"test fingerprint"}}
' https://{host}:{port}/{path}
```

Мерчант, используя публичный ключ и имея в распоряжении тело запроса, подпись, алгоритм подписи и хэширования, может произвести проверку подписи

[1]: https://github.com/rbkmoney/damsel/blob/master/proto/webhooker.thrift
[2]: https://github.com/rbkmoney/swag-webhook-events/blob/master/spec/swagger.yaml
