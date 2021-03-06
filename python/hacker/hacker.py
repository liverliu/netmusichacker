import logging
import web
import config
import requests
import json
import hashlib
import random

logger = logging.getLogger('route')
logger.setLevel(logging.INFO)
formatter = logging.Formatter('[%(asctime)s] {%(filename)s:%(lineno)d} %(levelname)s - %(message)s','%m-%d %H:%M:%S')
#console
ch = logging.StreamHandler()
ch.setLevel(logging.INFO)
ch.setFormatter(formatter)
logger.addHandler(ch)
#file
fh = logging.FileHandler('hacker.log')
fh.setLevel(logging.INFO)
fh.setFormatter(formatter)
logger.addHandler(fh)

urls = (
    '/.*', 'route',
    '/api/.*', 'route',
    '/eapi/.*', 'route',
)

valid_header = ['HTTP_ORIGIN', 'HTTP_COOKIE', 'HTTP_ACCEPT', 'HTTP_CONNECTION', 'HTTP_USER_AGENT',
                'HTTP_ACCEPT_LANGUAGE', 'HTTP_ACCEPT_ENCODING', 'CONTENT_LENGTH', 'CONTENT_TYPE',
                'HTTP_BATCH_METHOD', 'HTTP_REFERER']

new_header = {'HTTP_ORIGIN':'Origin', 'HTTP_COOKIE':'Cookie', 'HTTP_ACCEPT':'Accept',
              'HTTP_CONNECTION':'Connection', 'HTTP_USER_AGENT':'User-Agent', 'HTTP_HOST':'Host',
              'HTTP_ACCEPT_LANGUAGE':'Accept-Language', 'HTTP_ACCEPT_ENCODING':'Accept-Encoding',
              'CONTENT_LENGTH':'Content-Length', 'CONTENT_TYPE':'Content-Type', 'HTTP_BATCH_METHOD':'Batch-Method',
              'HTTP_REFERER':'Referer'}

response_headers = ['Content-Type', 'Connection', 'Pragrma', 'Cache-Control', 'Expires', 'Vary', 'Server', 'Date']

class MyApplication(web.application):
    def run(self, host='127.0.0.1', port=8080, *middleware):
        return web.httpserver.runsimple(self.wsgifunc(*middleware), (host, port))

class route:
    def GET(self):
        return handle()

    def POST(self):
        return handle()

def handle():
    logger.info('-------------------')
    logger.info(web.ctx.path)
    try:
        headers={}
        for k,v in web.ctx.env.items():
            if(k.upper()=='HTTP_HOST'):
                headers[new_header[k]]='music.163.com'
                continue
            if(k.upper() in valid_header):
                headers[new_header[k]] = v
        if web.ctx.env['REQUEST_METHOD'].upper() == 'POST':
            response = requests.post(config.host+web.ctx.path, data=web.data(), headers=headers)
        elif web.ctx.env['REQUEST_METHOD'].upper() == 'GET':
            response = requests.get(config.host+web.ctx.env['REQUEST_URI'], headers=headers)
        else:
            return None
        for k,v in response.headers.items():
            if k in response_headers:
                web.header(k, v)
        return modify(response.content)
    except Exception, ex:
        logger.error(ex)
        return None

def modify(message):
    try:
        result = json.loads(message)
        if web.ctx.path.startswith('/eapi/v1/album/'):
            logger.info('modify album info')
            if result['songs']:
                for song in result['songs']:
                    modify_privilege(song['privilege'])
                    if song['fee'] and song['fee']>0:
                        song['fee']=0

        elif web.ctx.path=='/eapi/v3/song/detail/':
            logger.info('modify songs privileges')
            map(modify_privilege, result['privileges']) if result['privileges'] else None

        elif web.ctx.path=='/eapi/v3/playlist/detail':
            logger.info('modify songs info')
            map(modify_privilege, result['privileges']) if result['privileges'] else None

        elif web.ctx.path=='/eapi/song/enhance/player/url':
            data = result['data'][0]
            if data['code'] != 200:
                logger.info('try to generate url: {}'.format(data['id']))
                song = music_detail(data['id'])
                music = get_music(song)
                data['code']=200
                data['type']='mp3'
                data['url']=gen_url(song)
                data['gain']=music['volumeDelta']
                data['br']=music['bitrate']
                data['size']=music['size']
                data['md5']=music['dfsId']
            logger.info(result)

        elif web.ctx.path=='/eapi/batch':
            logger.info('modify search result')
            search = result['/api/cloudsearch/pc']
            [modify_privilege(song['privilege']) for song in search['result']['songs']] if search['code']==200 else None

        elif web.ctx.path=='/eapi/cloudsearch/pc':
            logger.info('modify search result')
            [modify_privilege(song['privilege']) for song in result['result']['songs']] if result['code']==200 else None

        elif web.ctx.path.startswith('/eapi/v1/artist'):
            logger.info('modify singer info')
            [modify_privilege(hot_song['privilege']) for hot_song in result['hotSongs']]

        elif web.ctx.path.startswith('/eapi/song/enhance/download/url'):
            logger.info(message)

        return json.dumps(result)
    except Exception, ex:
        logger.info(ex)
        return message

def get_music(song):
    if(song['hMusic']):
        return song['hMusic']
    elif song['mMusic']:
        return song['mMusic']
    elif song['lMusic']:
        return song['lMusic']
    elif song['bMusic']:
        return song['bMusic']
    else:
        return song['audition']

def modify_privilege(privilege):
    if privilege:
        if privilege['st'] and privilege['st']<0:
            privilege['st']=0
            privilege['cs']=False
            privilege['subp']=1
            privilege['fl']=privilege['maxbr']
            privilege['dl']=privilege['maxbr']
            privilege['pl']=privilege['maxbr']
            privilege['sp']=7
            privilege['cp']=1
        if privilege['fee'] and privilege['fee']>0:
            privilege['fee']=0
            privilege['st']=0
            privilege['cs']=False
            privilege['subp']=1
            privilege['fl']=privilege['maxbr']
            privilege['dl']=privilege['maxbr']
            privilege['pl']=privilege['maxbr']
            privilege['sp']=7
            privilege['cp']=1

def music_detail(id):
    url = '{}/api/song/detail?ids=[{}]'.format(config.host, id)
    response = requests.get(url, headers=config.headers).text.encode('utf-8')
    song = json.loads(response)['songs'][0]
    if not song['hMusic'] and not song['mMusic'] and not song['lMusic'] and not song['bMusic']:
        album = album_detail(song)
        for song1 in album['songs']:
            if song1['id'] == song['id']:
                return song1
    return song

def album_detail(song):
    url = '{}/api/album/{}'.format(config.host, song['album']['id'])
    response = requests.get(url, headers=config.headers).text.encode('utf-8')
    return json.loads(response)['album']

def gen_url(song):
    music = get_music(song)
    song_id = music['dfsId']
    enc_id = encrypt(song_id)
    return 'http://m{}.music.126.net/{}/{}.mp3'.format(random.randint(1,2), enc_id, song_id)

def encrypt(id):
    magic = bytearray('3go8&$8*3*3h0k(2)2')
    song_id = bytearray(str(id))
    magic_len = len(magic)
    for i in xrange(len(song_id)):
        song_id[i] = song_id[i] ^ magic[i % magic_len]
    m = hashlib.md5(song_id)
    result = m.digest().encode('base64')[:-1]
    result = result.replace('/', '_')
    result = result.replace('+', '-')
    return result

app = MyApplication(urls, globals())
application = app.wsgifunc()

if __name__ == '__main__':
    app.run(host=config.server_host, port=config.server_port)