package object automata {

	// this is idiotic i'm sorry
	type TransitionList[S,A] = List[(A,S)]

	// is this necessary?
	// possibly not, unless we want to have a "visualize" function that would
	// actually take general automata or more specialized ones
	// if we do ever need it it'll be here lol
	trait Automaton[S,A] {
		def transition(s: S, a: A) : Option[S]
		def accept(initState: S, string: List[A]) : Boolean
	}

	trait StackOp[+A]
	case class Push[A](a: A) extends StackOp[A]
	case object Pop extends StackOp[Nothing]
	case object DoNothing extends StackOp[Nothing]
	case class PopPush[A](a: A) extends StackOp[A]
	
	trait StackHeadState[+A]
	case class Head[A](a: A) extends StackHeadState[A]
	case object Empty extends StackHeadState[Nothing]


	// constructs a pair
	implicit class Transition[S,A](a: A) {
		def ==> (s: S) = (a, s)
	}

	// alternate list constructor
	object << {
		def apply[A](a: A) = {
				List[A](a)
		}
	} 

	// not necessary but it makes things look much nicer
	// just a list identity function
	implicit class EndList[A](l: List[A]) {
		def >> = l
	}

	// fancy append operator
	implicit class AppendOperation[A](l: List[A]) {
		def __ (a: A) = l ++ List(a)
	}

}