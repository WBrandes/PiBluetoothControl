from flask import Flask,render_template
app = Flask(__name__)

@app.route("/")
def hello():
	return render_template("Home.html")

@app.route("/update/<password>")
def updatePassword(password):
    
    with open('/home/pi/Desktop/passwordStore.txt','w') as passFile:
        passFile.write(password)
    
    return render_template("Home.html")
