#!/usr/bin/env tarantool

log = require('log')
json = require('json')
lib_http_client = require('http.client')
lib_http_server = require('http.server')

box.cfg{
    listen = 3301,
    read_only = false,
    feedback_enabled = false
}
box.once('bootstrap', function()
    box.schema.user.create('replicator', {password = 'replicator'})
    box.schema.user.grant('replicator', 'replication')

    local subscribers = box.schema.space.create('subscribers')
    subscribers:format{
        {name = 'account_number', type = 'string'},
        {name = 'msisdn', type = 'string'}
    }
    subscribers:create_index('primary', {
        type = 'HASH',
        parts = {1, 'string'}
    })
end)

issvc_url_prefix = os.getenv('IMDB_ISSVC_URL') or 'http://localhost:5000/IS/v1'
http_addr = os.getenv('IMDB_HTTP_ADDR') or '0.0.0.0'
http_port = tonumber(os.getenv('IMDB_HTTP_PORT')) or 8000
url_path_prefix = os.getenv('URL_PATH_PREFIX') or '/IMDB/v1'

issvc_client = lib_http_client.new()

function handle_enrich_subscriber(self)
    local account_number = self:stash('account_number')
    local row = box.space.subscribers:get{account_number}
    local status

    if row == nil then
        local url = issvc_url_prefix .. string.format('/dbenrich/subscriber/%s', account_number)
        log.info('fetching from %s', url)

        local issvc_resp = issvc_client:get(url)
        if issvc_resp.status == 200 then
            local resp_data = json.decode(issvc_resp.body)
            local issvc_status = resp_data.status.code

            if issvc_status == 200 then
                local data = resp_data.data
                row = box.space.subscribers:insert{
                    data.account_number,
                    data.msisdn,
                }

            elseif issvc_status == 404 then
                status = {code = 404, message = 'not_found'}
            elseif issvc_status == 500 then
                status = {code = 503, message = 'issvc_error'}
            elseif issvc_status == 595 then
                status = {code = 503, message = 'issvc_unavailable'}
            elseif issvc_status == 400 then
                status = {code = 500, message = 'issvc_bad_rq'}
            else
                status = {code = 500, message = 'issvc_bad_resp'}
            end
        else
            status = {code = 500, message = 'issvc_unexpected_http_status'}
        end
    end

    if not status then
        status = {code = 200, message = 'ok'}
    end

    local result = {status = status}
    if row then
        result.data = {
            account_number = row[1],
            msisdn = row[2],
        }
    end

    return self:render{json = result}
end

function handle_health(self)
    return self:render{json = {status = {code = 200, message = 'ok'}}}
end

server = lib_http_server.new(http_addr, http_port)
server:route({path = url_path_prefix .. '/enrich/subscriber/:account_number', method = 'GET'}, handle_enrich_subscriber)
server:route({path = url_path_prefix .. '/health', method = 'GET'}, handle_health)
server:start()
