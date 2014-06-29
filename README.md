AsmEventBus
===========

*AsmEventBus* is an implementation of **event system** with asm. It is faster and more powerful {citation needed} than the *EventBus* of Google *Guava*.

What is ASM
-----------
*ASM* is a java library, which provides real-time bytecode modifying and generating. Its name is referred to *inline assembler*, <del>although I consider it Aya Shameimaru's Miniskirt...</del>

*AsmEventBus* is powered by *ASM*.

Why ASM
-----------
Well... We know that *Guava EventBus* uses "reflect" to invoke event listeners, right? And we also know reflect is slow. *AsmEventBus* will dynamically generate invoker class, to invoke event listeners by method calling, which is faster than reflect.

```
　　　　　　　　　　　　　　　　　　　 　 　 　 _」Lﾕﾆ=-=ﾆﾕZ」_
　　　　　　　　　　　　　　　 　 　 　 __≫''´ニニニニニニニﾆ｀`' ､
　　　　　　　　　　　　　　　　 　 __アﾞﾆニニニニニニﾆ=-‐ … ‐- ヽ
　　　　　　　　　　　　　 　 　 　 ｱニニニニﾆ=‐''^＾　　 　_,,. 　 ‐=＝'ニ二ニ＝=‐-　　.,,_
　　　　　　　　　　　　　　　　　/ニﾆア^ﾏ"´　　　 ,..｡s≦ニニニニニニニニ=-‐　''^＾三 ﾉ
　　　　　　　　　　　　 　 　 　 ﾆﾆｱﾞ　　 }　.｡s≦ニニニニニ=‐　　 　 `'≪,三三三ﾆ=‐
　　　　　　　　　　　　　____　 j{ﾆ7　 .．＜ニニニニﾆ=‐　　　　　　　　　　 `寸ﾆ=‐
　　　　　　　　　　　　(　　 　`{⌒*'”ニニニﾆ=‐　　　　　　　　 `' ､　 　 　 　 ﾏ
　　　　　　　　　　　　 ＼　　-=ﾆニニ=‐　　　　 　{　　　　 　 　 　 ＼　　　 　 ∨
　　　　　　　　　　　　　 ／ニニ=‐　　/　　.:}   :{　　　　　　 }i 　  ヽ　 　 　 }
　　　　　　　 　 　 　 ／ニ=‐　　  ./　　　　ﾉ 　:{　　　　　　 }ﾄ､　　　　　　　 ﾉ
　 　 　 　 　 　 　 ／ニﾆｱ　 /　　 /　乂ｰ '^　　 人　　　　　   从 ヽ 　  :i　　／
 　　 　 　 　 　 ／ニニｱﾞ　 /　 　′　　    ≧=-斗午㍉､ 　 　   ｧ示㍉　　  人　(
　　　　　 　 ／ニﾆﾆｱﾞ　　.′　　 　 　    {i:｛ ノ爪　　 )　  イ,ﾉﾊ   ｝⌒{`' ､｝
　　　 　 　 (=ニニｱﾞ　　　 　 　 i{ 　 　{i:｛乂 ッ　　'ﾞ´　 　ゞ  　ﾉ　 }　 i
 　 　 　 　 　 ¨¨ /　,:ﾞ　{　　　i{　　　从　乂　　　　　　 ,　　ｰ=彡　  }　 |
　　　　　　　'ﾞ｛ .′/　　i{　　　i{　　　＾沁､　 ¨´ﾉ　 　 　 　   从　   ﾉ　人 
　　　　　　 {　乂　′　　 从　　 .i{　　　 　 }今=-く　 　♉　 '　　.仏ィ^／
　　　 　 　 乂　　　 　 　 ,沁､ 　乂　　　　 }　　ｉ{≧ｧ- -=ｾ升　　    {（
　　　　　　　　｀¨ｉ{　 　 　 ⌒令=- ｀`　　,ﾉ 　 从/　　　 （人　    :{i
 　　　　 　 　 　 从. 　 　 　 从　　　ｰ=彡 　 　仏^＼ 　  }､ ｝＾＼  :{i  
　　　　　 　 　 　 会=- -=彡令s｡ 　     人ー‐匕_rzzミ､丶  ﾉ^ヽ｝　　 `    ASM♂EVENT♂BUS
　　　　　　　　　　　　　　　　 ｛_{⌒ﾆ=- `　_,ノｱﾞ⌒`'寸､)､       A library PM will loving delete.
　　　　　　　　　　　　　　　　　}_｝　　　　　j 7^　　　　ｰ '^' ､        Again and again.
　　　　　　　　　　　　　　　　 ｛_{　　　　 　}_｝　　　　　　　 ヽ
　　　　　　　　　　　　　　　　　}_｝　　　　｛_{　　　　　　　　　　｡ ,,jI斗-- ミ
　　　　　　　　　　　　 　 　 　 乂h､ 　 　 .}_｝　　　　　　  i. 　 }iｱﾞ　　 　 ｀寸
　　　　　　　　　　　　　　　　　　心､h､　 ﾉﾘ{　　　　　　　　  |    　7　　 　__,ｧ'Ⅶ　　　　   　 ∮
　　　　　　　　　　　　　　　　 ,&ﾟ⌒,心､彡'ﾞ^{　　　 　 　 　  |      {   　 , ｱ^　}i　　　   　  φ  
　　　　　 　 　 　 　 　 　 　 ,?　　　 沁　  {　　　　 　 :| 　     叭　  　  　 从　　　      ∂
　　　　　　　 　 　 　 　 　 ψ　 　 　  ｀沁　 }　　　　　　ｉ{　     沁､　∮　  ,.仆　　　　   ,?
　　　　　　　　 　 　 　 　 ＃　 　 　 　 小　　}　　　　　　ｉ{　　   i{令 &,==七I゛　 　 　 ,g'ﾟ
　　　　　　　　　　　　　　∮　　　　　　 }ﾉ　r'ﾞjI斗--　ミ　.从　　 ノ^' ､　‰｡　　　　,.c?ﾟ⌒
　　　　 　 　 　 　 　 　 ?　　　　  　　｛ ≫''゛　　　　　　`　沁,　　　　 ＼ ⌒ﾟ''∞''ﾟ⌒_
　　　　　　　　　　　　　　ﾟ&　　  　 ≫''゛　　-‐=‐-ミ　　　　沁,　　　　　i}　　　´　　　 ｀Y
　　　　jI斗　=＝=‐-　ミ　　 ？｡ アﾞ　　 　 　 'ﾞ⌒^　 ｀`' ､　⌒i}　　　　 ﾉ､,ノ　　　　　　    i
　__ア´　　　　　　　　　　｀`'＜_ｱﾞ　　　　　　　　　　　 　 　 　 〈　　　／／　　　　　　  　 ﾉ
. ｱﾞ　　　 　 　 　 　 　 　 　 　 ′　　　　　　　　　　　　 　 　 　 V ／／　　　　　　　　 i{

```

Where to download
--------
You can download the binary library from its CI.

http://ci.hakugyokurou.net/job/AsmEventBus/ws/AEB/build/libs/

Sorry but I haven't configure complete... I'm too busy in these days...

```
　　　　　　　　　 　　,.,.-　-‐─- ､.,_
　　　　　　　　 ,. '´　　　　　 　　　　｀'　、
　　　　　　　., '　　　　　　　,　　　　　　　ヽ、
　　　　　　 /　 　 . '´　 ／ l　　　　ﾊ　、 ﾊ｀フ       
　　　　　　,'　　　,'　　 ,' /__'、 　 |/_|_ ﾊ　'r'   
　　　　　　l　　 /　,　 .|/__」/_、　| _」/! }　 }
　　　　　　|　∠.イ|　　「 l)l::}｀ ＼|´l:l!}|/　八
　　　　　　',　　　八　 'ひ-'　 　　　`"oイ￣｀
　　　　　　 ',　　/　 l ､._,ゞ"　　 　　　"}ﾘ          ASM♂EVENT♂BUS
　　　　　　　}　 　　 |　 ﾄ 、 　　´ 　 ,.ｲ!          A QC's doomday.
　　　　　 　ﾉ　　　 八　',　} 　ｰｒｧ升|　|    They'll work overtime every day. 
　　　ﾄ-‐''´　　　 ,:'　,ﾊ　∨＼ _ﾊ ヽ!　|
　　　｀>'´　　　／/´ヽﾚ'､|｀}_アｒｒ'7ﾊ　|
　　／　　 　, '　　'　　 ∨　 　○　∨!八
　,:　' 　　 /　 　 {　　　 、  △　} | 　 ',
　{　　　　 ;　　 　∧ 　　 ヽ　 ×　| |　　 ,.
　 ､　　　　､　　 ;　 ',　 　　 ＼.八　'､!　 }
　　＼　　　 ＼　, く ＼　　　/｀7ヽYヽ　　;
　　　 )　 ノ,　／　　｀ア`' ､/　/__八,ﾊ}、/
　　 （r'´　(, '　 　　 ./ 　　 ｀ ´　ヽ　￣　ヽ
　 　 ｀　　 {　　　　 ;　　　　 　　　 ',　　 　ﾊ
　　　 　 ,-{ `　､.,＿､　　　　　　　　, 　 　　}
　　　　/ ,ｒ＞､.,_＿ ､＞-‐- ､.ノ　 ﾉ　 　 ｒﾉ、
　　 　 {_く_ｒ､＿,,.. -､＿,.. -､_＞イ__.＞-く__r'
　　 　 
```
