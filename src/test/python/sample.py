from getgauge.python import Messages
from getgauge.python import step


@step("This is a test to call python passing string <s> and integer <i>")
def sample(s, i):
    Messages.write_message("This is message from Python")
    Messages.write_message("Param values are {} and {}".format(s, i))
    assert i == 9
