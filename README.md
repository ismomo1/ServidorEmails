# ServidorEmails
Servidor de emails

Aplicación Java que expone una API REST para realizar operaciones CRUD emulando un servidor de emails.
Los endPoints están definidos bajo la ruta “/emails” y la información referente a los emails se almacenada en base de datos.

EndPoints de la API:
- GET "/emails": Endpoint para obtener uno o varios emails por id. Recibirá como parámetro los ids de los emails a obtener. La respuesta será un json con los emails que coincidan con los ids indicados. Ejemplo: http://localhost:8080/emails?ids=1435,1436,1437
- GET "/emails/all": Endpoint para obtener todos los emails. La respuesta será un json con todos los emails de base de datos. Ejemplo: http://localhost:8080/emails/all
- GET "/findByEmailFrom": Endpoint para filtrar por emailFrom. Recibirá como parámetro emailFrom de los emails a obtener. La respuesta será un json con los emails cuyo emailFrom coincida con el indicado. Ejemplo: http://localhost:8080/emails/findByEmailFrom?emailFrom=marcus@gbtec.es
- GET "/findByEmailTo": Endpoint para filtrar por un email contenido en emailTo. Recibirá como parámetro emailTo de los emails a obtener. La respuesta será un json con los emails cuyo emailTo coincida con el indicado. Ejemplo: http://localhost:8080/emails/findByEmailTo?emailTo=roger@gbtec.es
- GET "/findByEmailCC": Endpoint para filtrar por un email contenido en emailCC. Recibirá como parámetro emailCC de los emails a obtener. La respuesta será un json con los emails cuyo emailCC coincida con el indicado. Ejemplo: http://localhost:8080/emails/findByEmailCC?emailCC=michael@gbtec.es
- GET "/findByState": Endpoint para filtrar por estado. Recibirá como parámetro estado de los emails a obtener. La respuesta será un json con los emails cuyo estado coincida con el indicado. Ejemplo: http://localhost:8080/emails/findByState?state=1
- GET "/findByDateRange": Endpoint para obtener todos los emails de un rango de fechas de última actualización. Recibirá como parámetro emailTo de los emails a obtener. La respuesta será un json con los emails cuyo emailTo coincida con el indicado. Ejemplo: http://localhost:8080/emails/findByEmailTo?emailTo=roger@gbtec.es
- POST "/emails": Endpoint para crear uno o varios nuevos emails. Mandará en el cuerpo de la solicitud un json con el o los emails a insertar en base de datos. La respuesta será un json con los emails insertados en base de datos. Ejemplo: http://localhost:8080/emails. El formato del mensaje json será el siguiente:
  {
    "emails":[
        {
            "emailId":1435,
            "emailFrom":"marcus@gbtec.es",
            "emailTo":[
                {
                    "email":"andrew@gbtec.es"
                },
                {
                    "email":"peter@gbtec.es"
                }
            ],
            "emailCC":[
                {
                    "email":"carl@gbtec.es"
                }
            ],
            "emailBody":"Body text",
            "state":1
        },
        {
            "emailId":1436,
            "emailFrom":"marcus@gbtec.es",
            "emailTo":[
                {
                    "email":"roger@gbtec.es"
                },
                {
                    "email":"sam@gbtec.es"
                }
            ],
            "emailCC":[
                {
                    "email":"carl@gbtec.es"
                },
                {
                    "email":"michael@gbtec.es"
                }
            ],
            "emailBody":"Body text",
            "state":1
        }
    ]
}
   En dichos emails se deben cumplir varios requisitos para su inserción:
    -- El estado deberá ser un entero igual a 1(Enviado), 2(Borrador), 3(Eliminado) o 4(Spam).
    -- Deberá indicarse un id, el cual no puede existir ya en base de datos para otro email.
    -- El campo emailFrom no podrá omitirse.

- PUT "/deletedState": Endpoint para marcar el estado a eliminado en uno o varios emails. Recibirá como parámetro los ids de los emails a marcar. La respuesta será un json con los emails marcados. Ejemplo: http://localhost:8080/emails/deletedState?ids=1435,1436
- PUT "/sentState": Endpoint para marcar el estado a enviado en uno o varios emails con estado previo borrador. Recibirá como parámetro los ids de los emails a marcar. La respuesta será un json con los emails marcados. Ejemplo: http://localhost:8080/emails/sentState?ids=1437,1441
- PUT "/spamState": Endpoint para marcar el estado a spam en uno o varios emails. Recibirá como parámetro los ids de los emails a marcar. La respuesta será un json con los emails marcados. Ejemplo: http://localhost:8080/emails/spamState?ids=1435,1436
- PUT "/{id}": Endpoint para actualizar los campos emailTo, emailCC y/o emailBody de un email existente en estado borrador. Recibirá como parámetro el id del email a marcar. La respuesta será un json con el email modificado en caso de que haya sido posible hacerlo. Ejemplo: http://localhost:8080/emails/1443
- DELETE "/emails": Endpoint para eliminar uno o varios emails de base de datos. Recibirá como parámetro los ids de los emails a eliminar. La respuesta será un json con los emails eliminados. Ejemplo: http://localhost:8080/emails?ids=1435,1436
- DELETE "/deleteAll": Endpoint para eliminar todos los emails de base de datos. No recibirá ningún parámetro ni devolverá ninguna respuesta.

Consideraciones a tener en cuenta:
- Además de los datos propios de cada email, se guardará en base de datos la fecha de la última modificación realizada para cada uno de ellos.
- La base de datos configurada se encuentra en internet y tiene una limitación de 5 conexiones totales simultáneas. En caso de querer cambiar a otra base de datos, ya sea en red local o en internet, podrán modificarse sus parámetros (url, username, password) en el archivo application.properties.
- Al iniciar por primera vez la aplicación, se creará con Flyway la tabla de emails en base de datos junto con la tabla flyway_schema_history. Los archivos de migración de Flyway se encuentran en classpath:db/migration. El primer archivo generará la tabla emails. El segundo archivo insertará dos emails a modo de ejemplo.
- Configurada la publicación y consumo de mensajes colas RabbitMQ. La configuración del host, puerto, username y password se encuentran en el archivo application.properties. Será necesario tener levantado el servicio de RabbitMQ y habilitados los permisos para el usuario que se indique en el archivo de configuración. Los valores configurados por defecto son los siguientes:
      spring.rabbitmq.host=localhost
      spring.rabbitmq.port=5672
      spring.rabbitmq.username=guest
      spring.rabbitmq.password=guest
