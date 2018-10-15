#!/usr/bin/env python

import bluetooth
import gpiozero as gpio
import os
import sys
import hashlib
import threading
import random
import string

led = gpio.LED(2)
#try:
    #with open('/home/pi/Desktop/log.txt', 'a') as f:

passFile = open('/home/pi/Desktop/passwordStore.txt','r')
#The password.
password = passFile.read()
passFile.close()

#A string, the characters in it are used for generating random challenges. You can just append to this if you want more characters in the random generation.
challenge_chars = string.ascii_uppercase + string.ascii_uppercase + string.digits

#Amount of time inbetween challenges, in seconds
challenge_interval = 30.0


#Will be a random 32-character string made up of characters from challenge_chars. Updated every time a challenge is sent.
current_challenge = None
'''
Current hashes for:
 Password + current_challenge
 Password + current_challenge + '1'
 Password + current_challenge + '0'
 Respectively.
 on & off hashes are sent by client for turning the light on or off
'''

challenge_hash = None
challenge_hash_on = None
challenge_hash_off = None


def send_challenge():
    if send_challenges:
        #print(";)")
        
        passFile = open('/home/pi/Desktop/passwordStore.txt','r')
        password = passFile.read()
        passFile.close()
        
        global current_challenge
        
        global challenge_hash
        global challenge_hash_on
        global challenge_hash_off
        
        #Setting the timer up to run again
        #challenge_timer = threading.Timer(challenge_interval, send_challenge)
        #challenge_timer.daemon = True
        #challenge_timer.start()
        
        current_challenge= ''.join(random.choice(challenge_chars) for _ in range(32))
        #print(current_challenge)
        #Sending challenge to the client
        bytes = (current_challenge).encode("utf8")
        client_socket.send(bytes)
        #print("sent em")
        
        #Updating hash values        
        challenge_hash = hashlib.sha512(str.encode(password + current_challenge)).digest()
        challenge_hash_on = hashlib.sha512(str.encode(password + current_challenge + "1")).digest()
        challenge_hash_off = hashlib.sha512(str.encode(password + current_challenge + "0")).digest()
        #print(challenge_hash)
        #print("=====")
        #print(challenge_hash_on)
        #print("=====")
        #print(challenge_hash_off)
        #print("=====")

challenge_timer = None
send_challenges = False


while True:
    
    server_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    server_socket.bind(("",bluetooth.PORT_ANY))
    server_socket.listen(1)

    port = server_socket.getsockname()[1]

    UUID = "118e2222-23f8-41d9-8467-af70bc15f60d"

    bluetooth.advertise_service(server_socket, "PES",service_id=UUID, service_classes=[UUID, bluetooth.SERIAL_PORT_CLASS], profiles=[bluetooth.SERIAL_PORT_PROFILE])

    #print("Waiting for RFCOMM connection on channel %d" % port)
    #f.write("Waiting for RFCOMM connection on channel %d" % port)
    #f.write("\n")

    client_socket, client_info = server_socket.accept()
    
    #print("Accepted connection from " + str(client_info))
    
    challenge_timer = threading.Timer(0.1, send_challenge)
    challenge_timer.daemon = True
    challenge_timer.start()
    send_challenges = True
    
    
    try:
        while True:
            data = client_socket.recv(64)
            if len(data) == 0: break
            
            #print("received [%s]" % data)
            
            '''
            #print("------------------------")
            #print(challenge_hash)
            #print("------------------------")
            #print(challenge_hash_on)
            #print("------------------------")
            #print(challenge_hash_off)
            #print("------------------------")
            '''
            
            #Note that we call led.off() when they ask to turn it on, and vice versa for turning it off
            #This is simply because the relay inverts the logic of the signals we give it
            if data == challenge_hash:
                #Challenge passed
                #print("Challenge Good")
                pass
            elif data == challenge_hash_on:
                led.off()
                #print("ON")
            elif data == challenge_hash_off:
                led.on()
                #print("OFF")
            else:
                #Auth failed
                #print("Authentication Failed. Disconnecting.")
                send_challenges = False
                challenge_timer.cancel()
                
                client_socket.close()
                server_socket.close()
            
            '''
            number = 0
            #print(number)
            for b in data:
                number = number * 256 + int(b)
            if number == 1:
                led.on()
                bytes = ("Turned off LED").encode("utf8")
                if(led.is_active):
                    client_socket.send(bytes)
            else:
                led.off()
                bytes = ("Turned on LED").encode("utf8")
                if not (led.is_active):
                    client_socket.send(bytes)
            '''
    except IOError:
        pass

    #print("Device disconnect")
    
    send_challenges = False
    challenge_timer.cancel()

    client_socket.close()
    server_socket.close()