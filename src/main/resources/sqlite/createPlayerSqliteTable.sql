CREATE TABLE IF NOT EXISTS player
    (id text PRIMARY KEY,
     secret text,
     name text,
     faction text);

insert into player (id, secret) values ('2c2d4aea-7f38-11e5-a29f-069096004f69', '1118035f8f4160d5606e0c1a5e101ae5')
on conflict(id) do nothing;
