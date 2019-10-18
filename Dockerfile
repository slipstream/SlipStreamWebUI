FROM nginx:1.14-alpine

ADD resources/public /usr/share/nginx/html                    
ADD nginx/config/nginx.conf /etc/nginx
ADD nginx/config/default.conf /etc/nginx/conf.d
