# WJTEAM
김원준 정진우 손소연 강인지
##### 제목 들어가는 자리

제목 1 
======================

제목 2
----------------------

이탤릭체 *별표(asterisks)* or _언더바ㅏ(underscore)_
볼드 **별표(asterisks)** 혹은 __언더바(underscore)__
**_이탤릭체_와 두껍게** 같이
취소선 ~~물결(tilde)~~
<u>밑줄<u>은 '<u><u>'

1. 순서목록
  -비순서목록(서브)
  -비순서목록(서브)
2. 순서목록
  1. 순서목록(서브)
  2. 순서목록(서브)
3. 순서목록

비순서 목록
  ○대쉬(hyphen)
  ○별표(asterisks)
  ○더하기(plus sign)

링크
[구글](http://google.com)
[naver](http://naver.com "링크 설명(title)을 작성하세요.")
[상대적 참조](../users/login)
[dribbble][Dribbble link]
[GitHub][1]

문서내 [참조 링크]를 그대로 사용할 수도 있습니다.

다음과 같이 문서 내 일반 URL이나 꺾쇠 괄호('< >', Angle Brackets) 안의 URL은 자동으로 링크 사용
구글 홈페이지: https://google.co.kr
네이버 홈페이지: <http://naver.com>

[Dribbble link]: https://dribbble.com
[1]: https://github.com
[참조 링크]: https://naver.com "네이버로 이동합니다!"

##이미지

![대체 텍스트(alternative text)를 입력하세요!](http://gstatic.com/webp/gallery/5.jpg "링크설명(title) 작성

![kayak][logo]

[logo]: http://www.gstatic.com/webp/gallery/2.jpg "To go kayaking."

##이미지링크
[![Vue](/images/vue.png)](https://kr.vuejs.org/)

##코드 강조
'`' 입력
`background` 혹은 `background_image` 속성으로 요소에 배경 이미지 삽입


##블록코드강조
```html
<a href= "https://www.google.co.kr/" target="_blank">GOOGLE</a>
```

``` css
.list > li {
  position: absolute;
  top: 40px;
}
```

```javascript
fuction fuc() {
  var a = 'AAA';
  return a;
```

```bash
$ vim ./~zshrc
```

```python
s = "Python syntax highlighting"
print s
```

```
No language indicated, so no syntax highlighting.
But let's throw in a tag.
```

table

| 값 | 의미 | 기본값 |
|---|:---:|---:|
| `static` | 유형(기준) 없음 / 배치 불가능 | `static` |
| `relative` | 요소 자신을 기준으로 배치 |  |
| `absolute` | 위치 상 부모(조상)요소를 기준으로 배치 |  |
| `fixed` | 브라우저 창을 기준으로 배치 |  |

값 | 의미 | 기본값
---|:---:|---:
`static` | 유형(기준) 없음 / 배치 불가능 | `static`
`relative` | 요소 **자신**을 기준으로 배치 |
`absolute` | 위치 상 **_부모_(조상)요소**를 기준으로 배치 |
`fixed` | **브라우저 창**을 기준으로 배치 |

인용문(blockqoute)

> 남의 말이나 글에서 직접 또는 간접으로 따온 문장.
> _(네이버 국어 사전)_

BREAK!

> 인용문을 작성하세요!
>> 중첩된 인용문(nested blockquote)을 만들 수 있습니다.
>>> 중중첩1
>>> 중중첩2
>>> 중중첩3

수평선(Horizontal Rule)

---
'---'
***
'***'
___
'___'


줄바꿈
여기서<br>
아래로
