# FlowV2

## Usage

Make a new Flow by creating a ${name}.puml file (Plant UML).
The name should use same naming conventions as kotlin files, as it will be used during code generation.

Here is two examples

```
@startuml

[*] -> LoginForm

CheckCredentials : val formData: me.jameshunt.inmotiontestapplication.login.LoginFragment.LoginFormData

LoginForm ---> CheckCredentials
LoginForm --> Back

CheckCredentials --> GetProfile
CheckCredentials --> ShowError

ShowError --> LoginForm

GetProfile --> Done
GetProfile --> ShowError

@enduml
``` 

```
@startuml

SaveProfile : val formData: me.jameshunt.inmotiontestapplication.profile.Profile

Done : val formData: me.jameshunt.inmotiontestapplication.profile.Profile

[*] -> GetProfile
GetProfile --> ProfileRequest
ProfileRequest --> SaveProfile
SaveProfile --> Done
GetProfile --> Done

@enduml
```