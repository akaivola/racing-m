wifi.setphymode(wifi.PHYMODE_G)
wifi.setmode(wifi.STATION)
wifi.sta.connect()

uart.setup(0, 9600, 8, uart.PARITY_NONE, uart.STOPBITS_1,1)

sv = net.createServer(net.TCP, 30)
sv:listen(6000, function(c)
  c:on("receive", function(c, payload)
    c:send(payload)
    c:send("\n")
    if payload then
      uart.write(0, tonumber(payload))
      uart.write(0, 0x10)
    end
  end)
end)

print(wifi.sta.getip())
