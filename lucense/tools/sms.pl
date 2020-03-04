
use HTML::Entities;
use URI::Escape;
use LWP 5.64;

sub sendSMS {
    my ($APP,$content,@mob) = @_;

    $smsURL = 'http://api.myvaluefirst.com/psms/servlet/psms.Eservice2';
    $content = encode_entities($content, "\x80-\xff");

    $xmlData = "";
    $seq = 0;
    foreach (@mob) {
        $seq++;
        $xmlData .= <<XML;
<ADDRESS FROM="$APP" TO="$_" SEQ="$seq" TAG="$APP" />
XML
    }
    $xmlData = <<XML;
<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE MESSAGE SYSTEM "http://127.0.0.1/psms/dtd/messagev12.dtd" >
<MESSAGE VER="1.2">
<USER USERNAME="adichad02" PASSWORD="02api1206"/>
<SMS  UDH="0" CODING="1" TEXT="$content" PROPERTY="0" ID="2">
$xmlData</SMS>
</MESSAGE>
XML

    my @ns_headers = (
        'User-Agent' => 'Adichad.com/1.0',
    );

    if(!defined($browser)) { $browser = LWP::UserAgent->new; }

    my $response =  $browser->post($smsURL, { 'data' => $xmlData, 'action' => 'send'});
}

