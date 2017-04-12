# webhooker (hooker)
Сервис вебхуков

[![Build Status](http://ci.rbkmoney.com/buildStatus/icon?job=rbkmoney_private/hooker/master)](http://ci.rbkmoney.com/job/rbkmoney_private/job/hooker/job/master/)

1. Сервис предоставляет интерфейс для CAPI для создания, удаления вебхуков, установки/редактирования опций вебхука (таких как url, public/private keys - они генерируются сервисом, типы событий). 
2. Сервис поллит bustermaze на появление событий, по которым должны отправляться сообщения на вебхуки

Интерфейс для capi доступен по пути /hook

Для более подробного ознакомления со структурой объектов можно воспользоваться ссылкой на сам объект [webhooker.thrift][1]

### Типы событий, по которым отправляются сообщения на вебхуки

Сообщения отправляются на следующие события:

1. Создание инвойса
2. Изменение статуса инвойса
3. Создание платежа
4. Изменение статуса платежа

### Формат сообщения, отправляемого мерчанту

#### Пример отправки запроса мерчанту

###### Отправка данных
Запрос в формате JSON отправляется методом POST на url, указанный при создании вебхука.
Подпись идет в headers запроса.

Тело запроса содержит поля (все поля, кроме payment_id, обязательные):

Название | Описание | Пример
------------ | ------------- | -------------
**event_type** | тип эвента (инвойс или платеж) | invoice (payment)
**invoice_id** | номер инвойса | 2Dbs4d4Dw 
**payment_id** | номер платежа | fsee5562 
**shop_id** | идентификатор магазина | 55 
**amount** | сумма, в минорных единицах | 120000
**currency** | валюта (ISO 4217)| RUB
**created_at** | дата и время создания инвойса в формате UTC (RFC 3339) | 2011-07-01T09:00:00Z
**metadata** | данные, переданные мерчантом при создании инвойса | {"order_id":"my_order_id"}
**status** | статус инвойса(платежа) | для инвойса (unpaid, paid, cancelled, fulfilled); для платежа (pending, processed, captured, cancelled, failed)

Таймаут на соединение и на запрос составляет 10с.

[Cписок кодов состояния HTTP][3]


#### Пример формирования подписи
Тело запроса (JSON) подписывается и подпись добавляется как заголовок http-запроса. Имя заголовка - X-Signature.
Тело запроса для события "Инвойс создан" будет выглядеть так: 
```
{"event_type":"invoice","invoice_id":"45b69f6ab0","payment_id":null,"shop_id":1,"amount":6207,"currency":"RUB","created_at":"2017-04-10T21:53:09.271Z","metadata":{"type":"contentType","data":"dGVzdA==","fields":["TYPE","DATA"],"fieldMetaData":{"TYPE":{"fieldName":"type","requirementType":1,"valueMetaData":{"type":11,"typedefName":null,"binary":false,"container":false,"struct":false,"typedef":false}},"DATA":{"fieldName":"data","requirementType":1,"valueMetaData":{"type":11,"typedefName":null,"binary":true,"container":false,"struct":false,"typedef":false}}},"setType":true,"setData":true},"status":"unpaid"}
```
После чего строка подписывается приватным ключом RSA с алгоритмом хэширования SHA-256 и размером ключа 1024.

#### Пример запроса к мерчанту

```
curl -v -X POST 
-H "Content-Type: application/json; charset=utf-8" 
-H "cor2A+pQ9dOMwUq8U5xUeLwh9UQD3DtzHHxcC5pplChgwmRKH5LG9Wz/rnZ2GLwUzLelowisVre1aHUKGJSl/NSF0PWJSWfCbMVtKpMwA9ZRb4pcKVX/RBiczCUIX+gFVj/6G7qWXBCxbcBV/u/vn61rWFBYi1Knvmaov4kGvZo=" 
-d '{"event_type":"invoice","invoice_id":"45b69f6ab0","payment_id":null,"shop_id":1,"amount":6207,"currency":"RUB","created_at":"2017-04-10T21:53:09.271Z","metadata":{"type":"contentType","data":"dGVzdA==","fields":["TYPE","DATA"],"fieldMetaData":{"TYPE":{"fieldName":"type","requirementType":1,"valueMetaData":{"type":11,"typedefName":null,"binary":false,"container":false,"struct":false,"typedef":false}},"DATA":{"fieldName":"data","requirementType":1,"valueMetaData":{"type":11,"typedefName":null,"binary":true,"container":false,"struct":false,"typedef":false}}},"setType":true,"setData":true},"status":"unpaid"}
' https://{host}:{port}/{path}
```

Мерчант, используя публичный ключ и имея в распоряжении тело запроса, подпись, алгоритм подписи и хэширования, может произвести проверку подписи

[1]: https://github.com/rbkmoney/damsel/blob/master/proto/webhooker.thrift
