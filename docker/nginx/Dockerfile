FROM nginx:stable
RUN apt-get update && apt-get install -y unzip runit
RUN curl -O https://releases.hashicorp.com/consul-template/0.19.5/consul-template_0.19.5_linux_amd64.zip \
  && unzip consul-template_0.19.5_linux_amd64.zip -d /usr/local/bin
COPY nginx.conf /etc/consul-templates/nginx.conf
COPY nginx.service /etc/service/nginx/run
COPY consul-template.service /etc/service/consul-template/run
RUN chmod +x /etc/service/nginx/run /etc/service/consul-template/run \
  && rm -v /etc/nginx/conf.d/*
CMD ["/usr/bin/runsvdir", "/etc/service"]
