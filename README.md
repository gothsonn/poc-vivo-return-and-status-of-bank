# Poc Vivo Return And Status Of Bank

Start your server as an simple java application  

You can view the api documentation in swagger-ui by pointing to  
http://localhost:9000/ 

Change default port value in application.properties

## Run application in container docker
Run this is first command.
docker build -t publishes-billing-data . --no-cache

After run command
docker run -d -p 9000:9000 -t publishes-billing-data
 
