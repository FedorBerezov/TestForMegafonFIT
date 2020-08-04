FROM tarantool/tarantool:2.x-centos7 as TARANTOOLBUILD
RUN yum install -y git cmake make gcc
RUN tarantoolctl rocks install http

FROM tarantool/tarantool:2.x-centos7
COPY --from=TARANTOOLBUILD /opt/tarantool .
COPY ./imdb/imdb.lua .
EXPOSE 8000
CMD ["tarantool", "/opt/tarantool/imdb.lua"]
