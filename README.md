# `Silhouette REST MySQL Seed`

Example project for Play Framework that uses [Silhouette](https://github.com/mohiva/play-silhouette) for authentication and authorization, exposed REST API for sign-up, sign-in.

Heavily inspired by `https://github.com/adamzareba/play-silhouette-rest-slick` from `Adam Zareba`

## Basic usage

### Sign-up

```bash
$ curl http://localhost:9000/api/auth/register \
       --header 'Content-Type: application/json' \
       --data '{"email": "adam.zareba@test.pl", "password": "this!Password!Is!Very!Very!Strong!", "fullName": "Adam Zareba", "terms": true}' \
       --verbose
```

```
< HTTP/1.1 200 OK
< Content-Type: application/json; charset=utf-8
< X-Auth-Token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
  "expiresOn": "2017-10-06T07:49:27.238+02:00"
}
```

### Sign-in

_Not necessary just after the sign-up because you already have a valid token._

```bash
$ curl http://localhost:9000/api/auth/login \
       --header 'Content-Type: application/json' \
       --data '{"email": "adam.zareba@test.pl", "password": "this!Password!Is!Very!Very!Strong!"}' \
       --verbose
```

```
< HTTP/1.1 200 OK
< Content-Type: application/json; charset=utf-8
< X-Auth-Token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...

{
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
  "expiresOn": "2017-10-06T07:49:27.238+02:00"
}
```

### Secured Action with autorization

_capture the token_

```
$ export JWT_TOKEN=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

_The token must belong to a user with Admin role_

```bash
$ curl http://localhost:9000/api/badPassword --header X-Auth-Token:$JWT_TOKEN --verbose
```

```
< HTTP/1.1 200 OK
< Content-Type: application/json; charset=utf-8

{"result":"qwerty1234"}
```

### sign-out

_capture the token_

```
$ export JWT_TOKEN=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

_The token must belong to a user with Admin role_


```bash
$ curl http://localhost:9000/api/auth/logout --header X-Auth-Token:$JWT_TOKEN --verbose
```

```
< HTTP/1.1 200 OK
< Vary: Origin
< Referrer-Policy: origin-when-cross-origin, strict-origin-when-cross-origin
< X-Frame-Options: ALLOW-FROM http://*
< X-XSS-Protection: 1; mode=block
< X-Content-Type-Options: nosniff
< Content-Security-Policy: frame-src * ;
< X-Permitted-Cross-Domain-Policies: master-only
< Date: Mon, 06 May 2019 19:34:49 GMT
< Content-Type: application/json
< Content-Length: 36
< 
* Connection #0 to host localhost left intact
{"result":"logged out successfully"}
```

## Database reload

It is possible to reload database with based data with scripts:
[recreate.bat](database/recreate.bat) or [recreate.sh](database/recreate.sh)

Using Docker: see the [database](./database) section

## API documentation

Documentation is available under address: [REST API](http://localhost:9000/docs)

# License

The code is licensed under [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0). 

## Configuration 


* You must set the `APPLICATION SECRET`

https://www.playframework.com/documentation/2.7.x/ApplicationSecret

Otherwise you will get the below error

```
at com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticatorService$$anonfun$init$4.applyOrElse(JWTAuthenticator.scala:297)
Caused by: com.atlassian.jwt.exception.JwtMalformedSharedSecretException: Failed to create MAC signer with the provided secret key
	at com.atlassian.jwt.core.writer.NimbusJwtWriterFactory.createMACSigner(NimbusJwtWriterFactory.java:74)
Caused by: com.nimbusds.jose.KeyLengthException: The secret length must be at least 256 bits
	at com.nimbusds.jose.crypto.MACProvider.<init>(MACProvider.java:118)
```
