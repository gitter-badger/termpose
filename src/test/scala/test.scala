import collection.mutable.ArrayBuffer
import org.scalatest._
import util.{Try, Success, Failure}
class TermposeParserSpec extends WordSpec with Matchers with TryValues{
	def expectsEqual(input:String, output:String) ={
		val pinp = Termpose.parse(input).success.value
		assert(pinp.toString == output)
	}
	def expectsEqualMultiLine(input:String, output:String) ={
		val pinp = Termpose.parseMultiLine(input).success.value
		assert(pinp.toString == output)
	}
	
	"Parser" should{
		"generally work" in{
			expectsEqual("""
			a
			""", "a")
			expectsEqual("""
			a(b(c(d)))
			""", "(a (b (c d)))")
			expectsEqual("""
			a b
			""", "(a b)")
			expectsEqual("""
			a b c
			""", "(a b c)")
			expectsEqual("""
			a
			  b
			  c
			""", "(a b c)")
			expectsEqual("""
			a b
			  c
			""", "(a b c)")
			expectsEqual("""
			Aaron b""", "(Aaron b)")
			expectsEqual("""
			Aaron
				because
				always
					keep
					dogs
						in pens
				keep
				swimming
			""", "(Aaron because (always keep (dogs (in pens))) keep swimming)")
			expectsEqualMultiLine("""
			Oglomere
			Oglomore
			""", "(Oglomere Oglomore)")
			expectsEqual("""
			"horrid clown"
				make "my day"
				horrid clown
					jp awesome:ross
			""", "(\"horrid clown\" (make \"my day\") (horrid clown (jp (awesome ross))))")
			expectsEqual("""
			how about a "
				~M~U~L~T~I~L~I~N~E~
				~S~T~R~I~N~G~
			//"YOU LIKE?"
			""", "((how about a \"~M~U~L~T~I~L~I~N~E~\\n~S~T~R~I~N~G~\") (// \"YOU LIKE?\"))")
			expectsEqual("""
			hobobo
				green witches:
					glon
					mora
					bonell
				emero
			""", "(hobobo (green (witches glon mora bonell)) emero)")
			expectsEqual("""a(bb(aod(igjd(ss))
				ojfjf
				jjj(s:
					2""", "(a (bb (aod (igjd ss)) ojfjf (jjj (s 2))))")
		}
		
		"do S-expression syntax number one" in {
			expectsEqual("(a b (c d))", "(a b (c d))")
		}
		
		"do S-expression syntax with indents" in {
			expectsEqual("a (b c\n\td (e f g)", "(a (b c (d (e f g))))")
		}
		
		"handle weird whitespace" in{
			expectsEqual("""
			a (  b  (  c  )    )  
			      	d
			""", "(a (b (c)) d)")
		}
		
		"put indentation before colon overrun" in{
			expectsEqual("""
			f
				a:
				b
			""", "(f (a) b)")
		}
		
		"interpret windows style line endings correctly" in{
			expectsEqual("\nf\n\ta\r\n\tb", "(f a b)") //apparently multiline strings in scala ignore escape sequences.
		}
		
		"perceive double spaced lines in multiline strings properly" in{
			expectsEqual("f \"\n\ta\n\t\n\ta", "(f \"a\\n\\na\")")
		}
		
		"properly handle escaped unicode snowman" in{
			expectsEqual("""
			f "MERRY HOLIDAY \h\h\h\h\h\h
			""", "(f \"MERRY HOLIDAY ☃☃☃☃☃☃\")")
		}
		
		"be able to output as a json array" in{
			val pinp = Termpose.parse("f(p l a(9 32 87").success.value
			assert(pinp.jsonString == ("[\"f\",\"p\",\"l\",[\"a\",\"9\",\"32\",\"87\"]]"))
		}
		
		"put quoted terms adjacent to the term before them inside former term" in{
			expectsEqual("a\"b\"", "(a b)")
		}
		
		"insert adjacent quoted terms in a complex example" in {
			expectsEqual("""
				f do"alll"
					daba ab"moko
					daba dke:ab"moko
			""", "(f (do alll) (daba (ab moko)) (daba (dke (ab moko))))")
		}
		
		"handle contained quoted terms properly" in {
			expectsEqual("""
				a
					b c"
						things writ""", "(a (b (c \"things writ\")))")
		}
		
		"chain colons according to the spec" in {
			expectsEqual("""
				a:b:c:d:
					d
					d
			""", "(a (b (c (d d d))))")
		}
		
		"not contain next line with open parens without indentation" in{
			expectsEqual("A(B\nA(B", "((A B) (A B))")
		}
		
		"nest empty lists for chained colons" in {
			expectsEqual(":::", "((()))")
		}
		
		"contain thing in chained colons" in {
			expectsEqual(":::abio\n\tbobo", "((((abio))) bobo)")
		}
		
		"contain thing that contains indented colons in chained colons" in {
			expectsEqual(":::a:b(c d\n\tbobo", "((((a (b c d bobo)))))")
		}
		
		"kinda mostly translate termpose to XML" in {
			val poses = Termpose.parseMultiLine("html(head(style(.\"body{background-color:black;color:black}\")) body(div(-id(dersite))))").success.value
			assert(Termpose.translateTermposeToSingleLineXML(poses).success.value == "<html><head><style>body{background-color:black;color:black}</style></head><body><div id=\"dersite\"/></body></html>")
		}
		
		"respond discerningly to open and closed containing expressions" in {
			expectsEqual("a b\n\tn", "(a b n)") //open
			expectsEqual("a(b(c\n\tn", "(a (b c n))") //open
			expectsEqual("a\n\tn", "(a n)") //closed
			expectsEqual("a:b\n\tn", "((a b) n)") //closed
			expectsEqual("a(b)\n\tn", "((a b) n)") //closed
			expectsEqual("(a b)\n\tn", "((a b) n)") //closed
		}
		
		"further test root child extraction" in {
			expectsEqual("a b( c\n\td", "(a (b c d))")
		}
	}
}