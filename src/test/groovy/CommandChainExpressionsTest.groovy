Integer.metaClass.getPills = { -> delegate }

def chloroquinine = ''

def take(n1)
{
    [of: { drug ->
        [after: { String time ->
            System.out.println 'hello'
            System.out.println n1
            System.out.println time
        }]
    }]
}

take 2.pills of chloroquinine after "6 hours"



def directory(Map m = [:], String path, String test)
{
    System.out.println 'yoyoyo'
}


directory '/srv', 'ha',
    mode: '0644',
    group: "a",
    owner: "a",
    sudo: false