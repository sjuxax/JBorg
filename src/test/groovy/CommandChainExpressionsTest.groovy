Integer.metaClass.getPills = { -> delegate }

def chloroquinine = ''

def take(n1) {
    [of: { drug ->
        [after: { String time ->
            System.out.println 'hello'
            System.out.println n1
            System.out.println time
        }]
    }]
}

def then(String b)
{
    [test2: { String c -> }]
}

String test2(String a)
{
    return a
}

take 2.pills of chloroquinine after "6 hours"

then "ah" test2 "ha"