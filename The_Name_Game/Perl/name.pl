sub printVerse {
    $name = shift;
    $sb = lc($name);
    $x = ucfirst $sb;
    $x0 = substr $x, 0, 1;

    if ($x0 eq 'A' or $x0 eq 'E' or $x0 eq 'I' or $x0 eq 'O' or $x0 eq 'U') {
        $y = lc($x);
    } else {
        $y = substr $x, 1;
    }

    $b = 'b' . $y;
    $f = 'f' . $y;
    $m = 'm' . $y;

    if ($x0 eq 'B') {
        $b = $y;
    } elsif ($x0 eq 'F') {
        $f = $y;
    } elsif ($x0 eq 'M') {
        $m = $y;
    }

    print "$x, $x, bo-$b\n";
    print "Banana-fana fo-$f\n";
    print "Fee-fi-mo-$m\n";
    print "$x!\n\n";
}

@nameList = ( "Gary", "Earl", "Billy", "Felix", "Mary", "Steve" );

foreach $name (@nameList) {
    printVerse($name);
}
