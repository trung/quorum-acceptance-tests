from getgauge.python import step


@step("This is a test to call python passing string <s> and integer <i>")
def sample(s, i):
    print("This is the values {} and {}".format(s, i))
