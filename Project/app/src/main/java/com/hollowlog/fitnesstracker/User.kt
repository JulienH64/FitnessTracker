package com.hollowlog.fitnesstracker

import java.io.Serializable

class User : Serializable {

    public var name: String = ""
    public var email: String = ""

    constructor() {}

    constructor(name: String, email: String) {
        this.name = name
        this.email = email
    }

}