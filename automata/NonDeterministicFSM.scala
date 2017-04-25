package automata

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.DurationInt
import scala.util.{Try, Success, Failure}

/* 
 * FSM with a transition relation rather than function
 * and a parallelized accept function
 */

class NonDeterministicFSM[S, A](transitions: List[(S,List[(A,List[S])])], acceptStates: List[S]) {
  
  private val transitionMap = scala.collection.mutable.HashMap.empty[S, List[(A,List[S])]]
  private val states = scala.collection.mutable.ListBuffer.empty[S]
  
  transitions map (p => 
    transitionMap += (p._1 -> p._2)
  )
  
  transitionMap.keySet map (states += _)
  
  def accept(initState: S, seq: List[A]) = {
    acceptHelper(new NDState[S,A](initState, seq))
  }
  
  private def acceptHelper(ndState: NDState[S,A]): Boolean = {
    val currentState: S = ndState._1
    val inputString: List[A] = ndState._2
    
    if (inputString.length > 0) {
      transition(currentState, inputString.head) match {
        case Some(stateList) => {
          
          val ndStatesReachable = stateList map (new NDState(_, inputString.tail))
          
          /* Evaluate all possible paths and fold them up */
          (ndStatesReachable map futureAccept).
            foldLeft(false)((acc: Boolean, next: Future[Boolean]) => futureOr(acc, next))
        }
        case None => false
      }
    } else {
      acceptStates contains currentState
    }
  }

  @throws(classOf[Exception])
  private def futureOr(acc: Boolean, next: Future[Boolean]) = (acc, next.value) match {
  	case (a, Some(Success(n))) => a || n
  	case (a, Some(Failure(e))) => throw e
  	case (a, None) => Await.result(next, 10.seconds)
  }

  private def futureAccept(ndState: NDState[S,A]) = {
  	Future {
  		acceptHelper(ndState)
  	}
  }
  
  private def transition(s : S, a : A) = transitionMap get s match {
    case Some(transitionList) => {
      val transitions = transitionList filter (_._1 == a)
      if ((transitions length) == 0) {
        None
      } else {
        Some(transitions.head _2)
      }
    }
    case None => None
  }
}