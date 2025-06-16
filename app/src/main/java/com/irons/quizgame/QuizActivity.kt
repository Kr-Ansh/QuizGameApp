package com.irons.quizgame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.irons.quizgame.databinding.ActivityQuizBinding
import kotlin.random.Random

class QuizActivity : AppCompatActivity() {

    lateinit var quizBinding: ActivityQuizBinding

    val database = FirebaseDatabase.getInstance()
    val reference = database.reference.child("questions")

    var question = ""
    var answerA = ""
    var answerB = ""
    var answerC = ""
    var answerD = ""
    var correctAnswer = ""
    var questionCount = 0
    var questionNo = 0 // = 1 if u r accessing all the elements from the database and not using hashset

    var userAnswer = ""
    var userCorrect = 0
    var userWrong = 0

    lateinit var timer: CountDownTimer
    private val totalTime = 30000L
    var timerContinue = false
    var timeLeft = totalTime

    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val scoreRef = database.reference

    val questions = HashSet<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        quizBinding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(quizBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        do {
            val number = Random.nextInt(1, 31)
            questions.add(number)
        } while(questions.size < 10)

        gameLogic()

        quizBinding.btnNext.setOnClickListener {

            resetTimer()
            gameLogic()
        }

        quizBinding.btnFinish.setOnClickListener {

            sendScore()
        }

        quizBinding.option1.setOnClickListener {

            pauseTimer()

            userAnswer = "a"
            if(correctAnswer == userAnswer) {

                quizBinding.option1.setBackgroundColor(Color.GREEN)
                userCorrect++
                quizBinding.tvCorrectAnswer.text = userCorrect.toString()
            } else {

                quizBinding.option1.setBackgroundColor(Color.RED)
                userWrong++
                quizBinding.tvWrongAnswer.text = userWrong.toString()
                findAnswer()
            }

            disableClickableOfOptions()
        }

        quizBinding.option2.setOnClickListener {

            pauseTimer()

            userAnswer = "b"
            if(correctAnswer == userAnswer) {

                quizBinding.option2.setBackgroundColor(Color.GREEN)
                userCorrect++
                quizBinding.tvCorrectAnswer.text = userCorrect.toString()
            } else {

                quizBinding.option2.setBackgroundColor(Color.RED)
                userWrong++
                quizBinding.tvWrongAnswer.text = userWrong.toString()
                findAnswer()
            }

            disableClickableOfOptions()
        }

        quizBinding.option3.setOnClickListener {

            pauseTimer()

            userAnswer = "c"
            if(correctAnswer == userAnswer) {

                quizBinding.option3.setBackgroundColor(Color.GREEN)
                userCorrect++
                quizBinding.tvCorrectAnswer.text = userCorrect.toString()
            } else {

                quizBinding.option3.setBackgroundColor(Color.RED)
                userWrong++
                quizBinding.tvWrongAnswer.text = userWrong.toString()
                findAnswer()
            }

            disableClickableOfOptions()
        }

        quizBinding.option4.setOnClickListener {

            pauseTimer()

            userAnswer = "d"
            if(correctAnswer == userAnswer) {

                quizBinding.option4.setBackgroundColor(Color.GREEN)
                userCorrect++
                quizBinding.tvCorrectAnswer.text = userCorrect.toString()
            } else {

                quizBinding.option4.setBackgroundColor(Color.RED)
                userWrong++
                quizBinding.tvWrongAnswer.text = userWrong.toString()
                findAnswer()
            }

            disableClickableOfOptions()
        }
    }

    fun findAnswer() {

        when(correctAnswer) {

            "a" -> quizBinding.option1.setBackgroundColor(Color.GREEN)
            "b" -> quizBinding.option2.setBackgroundColor(Color.GREEN)
            "c" -> quizBinding.option3.setBackgroundColor(Color.GREEN)
            "d" -> quizBinding.option4.setBackgroundColor(Color.GREEN)
        }
    }

    fun disableClickableOfOptions() {

        quizBinding.option1.isClickable = false
        quizBinding.option2.isClickable = false
        quizBinding.option3.isClickable = false
        quizBinding.option4.isClickable = false
    }

    fun resetOptions() {

        quizBinding.option1.setBackgroundColor(Color.WHITE)
        quizBinding.option2.setBackgroundColor(Color.WHITE)
        quizBinding.option3.setBackgroundColor(Color.WHITE)
        quizBinding.option4.setBackgroundColor(Color.WHITE)

        quizBinding.option1.isClickable = true
        quizBinding.option2.isClickable = true
        quizBinding.option3.isClickable = true
        quizBinding.option4.isClickable = true
    }

    private fun startTimer() {

        timer = object: CountDownTimer(timeLeft, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                timeLeft = millisUntilFinished

                updateCountDownText()
            }

            override fun onFinish() {

                resetTimer()
                updateCountDownText()

                quizBinding.tvQuestion.text = "Sorry, Time is up! Continue with next question"
                timerContinue = false

                disableClickableOfOptions()
            }
        }.start()

        timerContinue = true
    }

    fun updateCountDownText() {

        val remainingTime: Int = (timeLeft/1000).toInt()
        quizBinding.tvTime.text = remainingTime.toString()
    }

    fun pauseTimer() {

        timer.cancel()
        timerContinue = false
    }

    fun resetTimer() {

        pauseTimer()
        timeLeft = totalTime
        updateCountDownText()
    }

    fun sendScore() {

        user?.let {

            val userUID = it.uid

            scoreRef.child("scores").child(userUID).child("correct").setValue(userCorrect)
            scoreRef.child("scores").child(userUID).child("wrong").setValue(userWrong).addOnSuccessListener {

                Toast.makeText(applicationContext, "Scores sent to Database successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@QuizActivity, ResultActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    fun gameLogic() {

        resetOptions()

        reference.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                questionCount = snapshot.childrenCount.toInt()

                // Retrieving all the questions from the database
                /* if(questionNo <= questionCount) {
                    question = snapshot.child(questionNo.toString()).child("q").value.toString()
                    answerA = snapshot.child(questionNo.toString()).child("a").value.toString()
                    answerB = snapshot.child(questionNo.toString()).child("b").value.toString()
                    answerC = snapshot.child(questionNo.toString()).child("c").value.toString()
                    answerD = snapshot.child(questionNo.toString()).child("d").value.toString()
                    correctAnswer =
                        snapshot.child(questionNo.toString()).child("answer").value.toString()

                    quizBinding.tvQuestion.text = question
                    quizBinding.option1.text = answerA
                    quizBinding.option2.text = answerB
                    quizBinding.option3.text = answerC
                    quizBinding.option4.text = answerD

                    quizBinding.progressBar.visibility = View.INVISIBLE
                    quizBinding.linearLayoutTopText.visibility = View.VISIBLE
                    quizBinding.linearLayoutQuestionAndOptions.visibility = View.VISIBLE
                    quizBinding.linearLayoutButtons.visibility = View.VISIBLE

                    startTimer()
                } else {

                    val dialogMessage = AlertDialog.Builder(this@QuizActivity)
                    dialogMessage.setTitle("Congratulations!")
                    dialogMessage.setMessage("You have answered all the questions.\nDo you want to see the result?")
                    dialogMessage.setCancelable(false)
                    dialogMessage.setPositiveButton("See Result") {dialogWindow, position ->
                        sendScore()
                    }
                    dialogMessage.setNegativeButton("Play Again") {dialogWindow, position ->
                        val intent = Intent(this@QuizActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    dialogMessage.create().show()
                } */

                // Retrieving a specified number of random questions from the database
                if(questionNo < questions.size) {
                    question = snapshot.child(questions.elementAt(questionNo).toString()).child("q").value.toString()
                    answerA = snapshot.child(questions.elementAt(questionNo).toString()).child("a").value.toString()
                    answerB = snapshot.child(questions.elementAt(questionNo).toString()).child("b").value.toString()
                    answerC = snapshot.child(questions.elementAt(questionNo).toString()).child("c").value.toString()
                    answerD = snapshot.child(questions.elementAt(questionNo).toString()).child("d").value.toString()
                    correctAnswer =
                        snapshot.child(questions.elementAt(questionNo).toString()).child("answer").value.toString()

                    quizBinding.tvQuestion.text = question
                    quizBinding.option1.text = answerA
                    quizBinding.option2.text = answerB
                    quizBinding.option3.text = answerC
                    quizBinding.option4.text = answerD

                    quizBinding.progressBar.visibility = View.INVISIBLE
                    quizBinding.linearLayoutTopText.visibility = View.VISIBLE
                    quizBinding.linearLayoutQuestionAndOptions.visibility = View.VISIBLE
                    quizBinding.linearLayoutButtons.visibility = View.VISIBLE

                    startTimer()
                } else {

                    val dialogMessage = AlertDialog.Builder(this@QuizActivity)
                    dialogMessage.setTitle("Congratulations!")
                    dialogMessage.setMessage("You have answered all the questions.\nDo you want to see the result?")
                    dialogMessage.setCancelable(false)
                    dialogMessage.setPositiveButton("See Result") {dialogWindow, position ->
                        sendScore()
                    }
                    dialogMessage.setNegativeButton("Play Again") {dialogWindow, position ->
                        val intent = Intent(this@QuizActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    dialogMessage.create().show()
                }

                questionNo++
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(this@QuizActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}